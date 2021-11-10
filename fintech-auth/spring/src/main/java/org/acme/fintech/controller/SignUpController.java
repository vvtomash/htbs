package org.acme.fintech.controller;

import org.acme.fintech.exception.ClientConflictException;
import org.acme.fintech.exception.ClientValidationException;
import org.acme.fintech.exception.PasswordValidationException;
import org.acme.fintech.gateway.GatewayService;
import org.acme.fintech.gateway.SmsService;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.Device;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.DeviceRepository;
import org.acme.fintech.request.ClientDataRequest;
import org.acme.fintech.request.SignUpComplete;
import org.acme.fintech.response.JwtAuthenticationToken;
import org.acme.fintech.util.ModelUtils;
import org.acme.fintech.util.TokenUtils;
import org.hibernate.mapping.Bag;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @PostMapping
    public ResponseEntity<String> initiate(@RequestBody ClientDataRequest request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        try {
            Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
            if (client == null) {
                String msg = String.format("No client found for phone=%s, contract=%s, birthdate=%s", phone, contract, birthdate);
                throw new ClientValidationException(msg);
            } else if (Client.Status.ACTIVE == client.getStatus()) {
                throw new ClientValidationException(String.format("%s already activated", client));
            }

            // Generate and persist OTP token
            OtpToken optToken = ModelUtils.newOtpToken();
            client.setOtpToken(optToken);
            clientRepository.save(client);

            // Send OTP token
            GatewayService gateway = new SmsService();
            gateway.send(phone, optToken.getCode());

            return ResponseEntity.accepted().build();
        } catch (ClientValidationException ex) {
            logger.error("Signup initiate failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception ex) {
            logger.error("Signup initiate failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping
    @Transactional
    public ResponseEntity<JwtAuthenticationToken> complete(@RequestBody SignUpComplete request) {
        try {
            String phone = request.getPhone();
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                throw new ClientValidationException(String.format("No client found by phone=%s", phone));
            } else if (Client.Status.ACTIVE == client.getStatus()) {
                throw new ClientConflictException(String.format("%s already activated", client));

            }

            // Validate OTP Token
            OtpToken optToken = client.getOtpToken();
            TokenUtils.validateOtpToken(optToken, request.getOtpCode());

            // Generate credential
            Credential credential = ModelUtils.newCredential(client, request.getPassword());
            credential = credentialRepository.save(credential);

            // Create device
            Device device = Device.builder()
                    .pushToken(request.getPushToken())
                    .publicKey(request.getPublicKey())
                    .deviceId(request.getDeviceId())
                    .isActive(true)
                    .client(client)
                    .build();
            deviceRepository.save(device);

            // Activate client and set credential
            client.setOtpToken(null);
            client.setStatus(Client.Status.ACTIVE);
            client.setCredential(credential);
            clientRepository.save(client);

            // New JWT token
            JwtAuthenticationToken authToken = TokenUtils.newJWSToken(client);
            return ResponseEntity.ok(authToken);
        } catch (ClientValidationException | PasswordValidationException ex) {
            logger.error("Signup complete failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ClientConflictException ex) {
            logger.error("Signup complete failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception ex) {
            logger.error("Signup complete failed: " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
