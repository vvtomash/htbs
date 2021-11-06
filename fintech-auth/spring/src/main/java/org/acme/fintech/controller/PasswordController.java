package org.acme.fintech.controller;

import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.gateway.MessageService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.OtpTokenRepository;
import org.acme.fintech.request.FullContractRequest;
import org.acme.fintech.request.ResetPasswordComplete;
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

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/auth/password")
public class PasswordController extends AbstractController {
    private static final Logger logger = Logger.getLogger(PasswordController.class);

//    @Autowired
//    private ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private ClientRepository clientRepository;

    @PostConstruct
    public void init() {
    }

    @Transactional
    @PostMapping("/reset/initiate")
    public ResponseEntity<String> initiate(@RequestBody FullContractRequest request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        try {
            Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
            if (client == null) {
                logger.warn(String.format("Cannot reset password. No client found for phone=%s, contract=%s, birthdate=%s", phone, contract, birthdate));
                return ResponseEntity.badRequest().build();
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
            logger.error("Reset password failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Transactional
    @PostMapping("/reset/complete")
    public ResponseEntity<String> complete(@RequestBody ResetPasswordComplete request) {
        try {
            String phone = request.getPhone();
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                logger.warn(String.format("Cannot complete signup. No client found by phone=%s", phone));
                return ResponseEntity.badRequest().build();
            }

            // Validate OTP Token
            OtpToken otpToken = client.getOtpToken();
            validateOtpToken(otpToken, request.getOtpCode());

            // Generate credential
            Credential credential = newCredential(client, request.getPassword());
            credential = credentialRepository.save(credential);

            // Activate credential
            client.setOtpToken(null);
            client.setCredential(credential);
            clientRepository.save(client);

            // Remove OtpToken
            otpTokenRepository.delete(otpToken);

            return ResponseEntity.accepted().build();
        } catch (TokenValidationException ex) {
            logger.error("Reset password completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            logger.error("Reset password completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
