package org.acme.fintech.controller;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.request.VerifyPassword;
import org.acme.fintech.util.PasswordUtil;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = Logger.getLogger(ApiController.class);

    @Autowired
    private ClientRepository clientRepository;

    @PostConstruct
    public void init() {
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody VerifyPassword request) {
        try {
            String phone = request.getPhone();
            Client client = clientRepository.findByPhone(phone);
            if (client == null) {
                logger.warn("No client found for " + phone);
                return ResponseEntity.badRequest().build();
            } else if (Client.Status.ACTIVE != client.getStatus()) {
                logger.warn("Client not activated for " + phone);
                return ResponseEntity.badRequest().build();
            }

            Credential credential = client.getCredential();
            if (credential == null) {
                logger.warn("No credential found for " + phone);
                return ResponseEntity.badRequest().build();
            }

            // Encode given password using restored salt
            String password = request.getPassword();
            byte[] saltBytes = PasswordUtil.decodeBase64(credential.getSalt());
            String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);

            // Verify credential
            if (credential.getPassword().equals(encodedPassword)) {
                logger.debug("Password is correct!");
                return ResponseEntity.ok().build();
            } else {
                logger.warn("Password is wrong!");
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception ex) {
            logger.error("api verify failed " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
