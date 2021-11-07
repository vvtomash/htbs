package org.acme.fintech.controller;

import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.gateway.GatewayService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.request.FullContractRequest;
import org.acme.fintech.request.ResetPasswordComplete;
import org.acme.fintech.util.ModelUtils;
import org.acme.fintech.util.TokenUtils;
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

@RestController
@RequestMapping("/auth/password")
public class PasswordController {
    private static final Logger logger = Logger.getLogger(PasswordController.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/reset/initiate")
    public ResponseEntity<String> initiate(@RequestBody FullContractRequest request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        try {
            Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
            if (client == null) {
                logger.warn(String.format("Cannot reset password. No client found for phone=%s, contract=%s, birthdate=%s", phone, contract, birthdate));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generate and persist OTP token
            OtpToken optToken = ModelUtils.newOtpToken();
            client.setOtpToken(optToken);
            clientRepository.save(client);

            // Send OTP token
            GatewayService gateway = new SmsService();
            gateway.send(phone, optToken.getCode());

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
                logger.warn(String.format("Reset password completion failed. No client found by phone=%s", phone));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate OTP Token
            TokenUtils.validateOtpToken(client.getOtpToken(), request.getOtpCode());

            // Generate credential
            Credential credential = ModelUtils.newCredential(client, request.getPassword());
            credential = credentialRepository.save(credential);

            // Activate credential
            client.setOtpToken(null);
            client.setCredential(credential);
            clientRepository.save(client);

            return ResponseEntity.accepted().build();
        } catch (TokenValidationException ex) {
            logger.error("Reset password completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception ex) {
            logger.error("Reset password completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
