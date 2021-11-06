package org.acme.fintech.controller;

import org.acme.fintech.gateway.MessageService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.*;
import org.acme.fintech.repository.*;
import org.acme.fintech.request.SignUpComplete;
import org.acme.fintech.request.SignUpInitiate;
import org.acme.fintech.util.PasswordUtil;
import org.acme.fintech.util.RandomString;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController()
@RequestMapping("/auth/signup")
public class SignUpController {
    private static final Logger logger = Logger.getLogger(SignUpController.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SignUpRepository signUpRepository;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    @PostMapping("/initiate")
    public ResponseEntity<String> initiate(@RequestBody SignUpInitiate request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        try {
            Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
            if (client == null) {
                logger.warn(String.format("Cannot initiate signup. No client found for phone=%s, contract=%s, birthdate=%s", phone, contract, birthdate));
                return ResponseEntity.badRequest().build();
            } else if (Client.Status.ACTIVE == client.getStatus()) {
                logger.warn(String.format("Cannot initiate signup. Client %s already activated", client));
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Generate credential
            String password = request.getPassword();
            byte[] saltBytes = PasswordUtil.randomSalt();
            String saltString = PasswordUtil.encodeBase64(saltBytes);
            String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);

            // Assign/update credential in the client
            Credential credential = new Credential();
            credential.setCreateDateTime(LocalDateTime.now(ZoneOffset.UTC));
            credential.setPassword(encodedPassword);
            credential.setSalt(saltString);
            credential = credentialRepository.save(credential);

            // Generate and persist OTP token
            String code = RandomString.randomCode(6).toUpperCase();
            OtpToken otpToken = new OtpToken();
            otpToken.setCreateDateTime(LocalDateTime.now(ZoneOffset.UTC));
            otpToken.setExpiryDateTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)); // TODO Configuration
            otpToken.setCode(code);
            otpToken = otpTokenRepository.save(otpToken);

            // Persist sign up. Link to token and client
            SignUp signUp = new SignUp();
            signUp.setCredential(credential);
            signUp.setOtpToken(otpToken);
            signUpRepository.save(signUp);

            // Persist client
            client.setSignUp(signUp);
            clientRepository.save(client);

            // Send OTP token
            MessageService gateway = new SmsService();
            gateway.send(phone, code);

            return ResponseEntity.accepted().build();
        } catch (Exception ex) {
            logger.error("Signup initiate failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Transactional
    @PostMapping("/complete")
    public ResponseEntity<String> complete(@RequestBody SignUpComplete request) {
        String phone = request.getPhone();
        Client client = clientRepository.findByPhone(phone);
        if (client == null) {
            logger.warn(String.format("Cannot complete signup. No client found by phone=%s", phone));
            return ResponseEntity.badRequest().build();
        } else if (Client.Status.ACTIVE == client.getStatus()) {
            logger.warn(String.format("%s already activated", client));
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Sign up action shall be present with otp token assigned
        SignUp signUp = client.getSignUp();
        if (signUp == null) {
            logger.warn(String.format("Cannot complete signup. No signup found for %s", client));
            return ResponseEntity.badRequest().build();
        }

        // Validate OTP Token
        OtpToken otpToken = signUp.getOtpToken();
        LocalDateTime nowDateTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiryDateTime = otpToken.getExpiryDateTime();
        if (nowDateTime.isAfter(expiryDateTime)) {
            logger.warn(String.format("Cannot complete signup. OTP Token expired for %s", client));
            return ResponseEntity.badRequest().build();
        }
        if (!otpToken.getCode().equals(request.getOtpToken())) {
            logger.warn(String.format("Cannot complete signup. OTP tokens mismatch for %s", client));
            return ResponseEntity.badRequest().build();
        }

        // Create device
        Device device = Device.builder()
                .pushToken(request.getPushToken())
                .publicKey(request.getPublicKey())
                .isActive(true)
                .client(client)
                .build();
        deviceRepository.save(device);

        // Activate client along credential remapping
        client.setSignUp(null);
        client.setStatus(Client.Status.ACTIVE);
        client.setCredential(signUp.getCredential());
        clientRepository.save(client);

        // Remove SignUp and OtpToken
        signUpRepository.delete(signUp);
        otpTokenRepository.delete(otpToken);

        // TODO Generate JWT token

        return ResponseEntity.accepted().build();
    }
}
