package org.acme.fintech.controller;

import org.acme.fintech.exception.ClientValidationException;
import org.acme.fintech.exception.PasswordValidationException;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.request.VerifyPassword;
import org.acme.fintech.util.PasswordUtil;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                throw new ClientValidationException("No client found for " + phone);
            } else if (Client.Status.ACTIVE != client.getStatus()) {
                throw new ClientValidationException("Client not activated for " + phone);
            }

            Credential credential = client.getCredential();
            PasswordUtil.validatePassword(credential, request.getPassword());

            logger.debug("Password is correct!");
            return ResponseEntity.ok().build();
        } catch (ClientValidationException ex) {
            logger.error("Client validation failed " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (PasswordValidationException ex) {
            logger.error("Password validation failed " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception ex) {
            logger.error("API verify failed " + ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
