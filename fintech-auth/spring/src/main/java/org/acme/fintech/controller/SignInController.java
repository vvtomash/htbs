package org.acme.fintech.controller;

import org.acme.fintech.exception.ClientValidationException;
import org.acme.fintech.exception.PasswordValidationException;
import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.gateway.FcmService;
import org.acme.fintech.gateway.GatewayService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.DeviceRepository;
import org.acme.fintech.request.SignInComplete;
import org.acme.fintech.request.SignInRequest;
import org.acme.fintech.response.JwtAuthenticationToken;
import org.acme.fintech.util.ModelUtils;
import org.acme.fintech.util.PasswordUtil;
import org.acme.fintech.util.TokenUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth/signin")
public class SignInController {
    private static final Logger logger = Logger.getLogger(SignInController.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping
    public ResponseEntity<String> request(@RequestBody SignInRequest request) {
        String phone = request.getPhone();
        try {
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                throw new ClientValidationException(String.format("No client found for phone=%s", phone));
            } else if (Client.Status.ACTIVE != client.getStatus()) {
                throw new ClientValidationException(String.format("Client %s not activated", client));
            }

            // Validate password first
            Credential credential = client.getCredential();
            PasswordUtil.validatePassword(credential, request.getPassword());

            // Generate and persist OTP token
            OtpToken optToken = ModelUtils.newOtpToken();
            client.setOtpToken(optToken);
            clientRepository.save(client);

            // Send OTP token
            GatewayService gateway = new FcmService();
            gateway.send(phone, optToken.getCode());

            return ResponseEntity.accepted().build();
        } catch (ClientValidationException | PasswordValidationException ex) {
            logger.error("SingIn initiating failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception ex) {
            logger.error("SingIn initiating failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping
    public ResponseEntity<JwtAuthenticationToken> complete(@RequestBody SignInComplete request) {
        String phone = request.getPhone();
        try {
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                throw new ClientValidationException(String.format("No client found by phone=%s", phone));
            } else if (Client.Status.ACTIVE != client.getStatus()) {
                throw new ClientValidationException(String.format("Client %s is not activated", client));
            }

            // Validate OTP Token
            TokenUtils.validateOtpToken(client.getOtpToken(), request.getOtpCode());

            // Activate client and set credential
            client.setOtpToken(null);
            clientRepository.save(client);

            // New JWT token
            JwtAuthenticationToken authToken = TokenUtils.newJWSToken(client);
            return ResponseEntity.ok(authToken);
        } catch (ClientValidationException | TokenValidationException ex) {
            logger.error("Signin completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception ex) {
            logger.error("Signin completion failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
