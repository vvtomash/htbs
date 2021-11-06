package org.acme.fintech.controller;

import org.acme.fintech.gateway.MessageService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.Device;
import org.acme.fintech.model.SignUp;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.DeviceRepository;
import org.acme.fintech.repository.SignUpRepository;
import org.acme.fintech.request.SignUpComplete;
import org.acme.fintech.request.SignUpInitiate;
import org.acme.fintech.util.PasswordUtil;
import org.acme.fintech.util.RandomString;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private DeviceRepository deviceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SignUpRepository signUpRepository;

    //TODO DB Transactions

    @PostMapping("/initiate")
    public ResponseEntity<String> initiate(@RequestBody SignUpInitiate request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
        if (client == null) {
            logger.warn(String.format("No client found for phone=%s, contract=%s, birthdate=%s", phone, contract, birthdate));
            return ResponseEntity.badRequest().build();
        }

        // Generate disabled credential
        String password = request.getPassword();
        byte[] saltBytes = PasswordUtil.randomSalt();
        String saltString = PasswordUtil.encodeBase64(saltBytes);
        String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);
        Credential credential = Credential.builder()
                .createDateTime(LocalDateTime.now(ZoneOffset.UTC))
                .password(encodedPassword)
                .salt(saltString)
                .isActive(false)
                .client(client)
                .build();
        credential = credentialRepository.save(credential);

        // TODO Consider uniques as much as possible
        // Generate OTP token
        String otpToken = RandomString.randomCode(6).toUpperCase();

        // Persist sign up. Link to credential and client
        SignUp signUp = SignUp.builder()
                .createDateTime(LocalDateTime.now(ZoneOffset.UTC))
                .credential(credential)
                .otpToken(otpToken)
                .isActive(true)
                .client(client)
                .build();
        signUp = signUpRepository.save(signUp);

        // Assign signup to the client.
        // Credentials will be assigned upon sign up completion
        client.setSignUp(signUp);

        // Update client
        clientRepository.save(client);

        // Send OTP token
        MessageService gateway = new SmsService();
        gateway.send(phone, otpToken);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<String> complete(@RequestBody SignUpComplete request) {
        String phone = request.getPhone();
        Client client = clientRepository.findByPhone(phone);
        if (client == null) {
            logger.warn(String.format("No client found for phone=%s", phone));
            return ResponseEntity.badRequest().build();
        }

        // Sign up action shall be present in active state
        SignUp signUp = client.getSignUp();
        if (signUp == null) {
            logger.warn(String.format("No signup assigned to %s", client));
            return ResponseEntity.badRequest().build();
        } else if (!signUp.isActive()) {
            logger.warn(String.format("Signup deactivated for %s", client));
            return ResponseEntity.badRequest().build();
        }

        // Credential shall be present in disabled state
        Credential credential = signUp.getCredential();
        if (credential == null) {
            logger.error(String.format("No credential found for %s", signUp));
            return ResponseEntity.internalServerError().build();
        }

        // Validate OTP tokens
        if (!signUp.getOtpToken().equals(request.getOtpToken())) {
            logger.warn(String.format("OTP tokens mismatch for %s", client));
            return ResponseEntity.badRequest().build();
        }

        // Deactivate signup
        signUp.setActive(false);
        signUpRepository.save(signUp);

        // Create device
        Device device = Device.builder()
                .activateDateTime(LocalDateTime.now(ZoneOffset.UTC))
                .pushToken(request.getPushToken())
                .publicKey(request.getPublicKey())
                .isActive(true)
                .client(client)
                .build();
        deviceRepository.save(device);

        // Activate credential
        credential.setActive(true);
        credential.setActivateDateTime(LocalDateTime.now(ZoneOffset.UTC));
        credentialRepository.save(credential);

        // Remove sign up on client
        client.setSignUp(null);
        client.setCredential(credential);
        clientRepository.save(client);

        // Generate JWT token

        return ResponseEntity.accepted().build();
    }
}
