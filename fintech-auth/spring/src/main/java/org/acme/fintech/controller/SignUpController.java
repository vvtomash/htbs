package org.acme.fintech.controller;

import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.gateway.MessageService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.Device;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.DeviceRepository;
import org.acme.fintech.repository.OtpTokenRepository;
import org.acme.fintech.request.FullContractRequest;
import org.acme.fintech.request.SignUpComplete;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.acme.fintech.util.ModelUtils.newCredential;
import static org.acme.fintech.util.TokenUtils.validateOtpToken;

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

    @Transactional
    @PostMapping("/initiate")
    public ResponseEntity<String> initiate(@RequestBody FullContractRequest request) {
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

            // Generate and persist OTP token
            String code = RandomString.randomCode(6).toUpperCase();
            OtpToken otpToken = new OtpToken();
            otpToken.setCreateDateTime(LocalDateTime.now(ZoneOffset.UTC));
            otpToken.setExpiryDateTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)); // TODO Configuration
            otpToken.setCode(code);
            otpToken = otpTokenRepository.save(otpToken);

            // Persist client
            client.setOtpToken(otpToken);
            clientRepository.save(client);

            // Send OTP token
            MessageService gateway = new SmsService();
            gateway.send(phone, code);

            return ResponseEntity.accepted().build();
        } catch (Exception ex) {
            logger.error("Signup initiating failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Transactional
    @PostMapping("/complete")
    public ResponseEntity<String> complete(@RequestBody SignUpComplete request) {
        try {
            String phone = request.getPhone();
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                logger.warn(String.format("Cannot complete signup. No client found by phone=%s", phone));
                return ResponseEntity.badRequest().build();
            } else if (Client.Status.ACTIVE == client.getStatus()) {
                logger.warn(String.format("%s already activated", client));
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Validate OTP Token
            OtpToken otpToken = client.getOtpToken();
            validateOtpToken(otpToken, request.getOtpCode());

            // Generate credential
            Credential credential = newCredential(client, request.getPassword());
            credential = credentialRepository.save(credential);

            // Create device
            Device device = Device.builder()
                    .pushToken(request.getPushToken())
                    .publicKey(request.getPublicKey())
                    .isActive(true)
                    .client(client)
                    .build();
            deviceRepository.save(device);

            // Activate client and set credential
            client.setOtpToken(null);
            client.setStatus(Client.Status.ACTIVE);
            client.setCredential(credential);
            clientRepository.save(client);

            // Remove OtpToken
            otpTokenRepository.delete(otpToken);

            // TODO Generate JWT token

            return ResponseEntity.accepted().build();
        } catch (TokenValidationException ex) {
            logger.error("Signup completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            logger.error("Signup completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
