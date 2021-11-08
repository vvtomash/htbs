package org.acme.fintech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class AbstractController {
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ClientRepository clientRepository;

    @Autowired
    protected CredentialRepository credentialRepository;

    protected Client setupClient() {
        Client client = Client.builder()
                .phone("P1")
                .contract("C1")
                .status(Client.Status.ACTIVE)
                .birthdate(LocalDate.now())
                .build();
        return clientRepository.save(client);
    }

    protected void setupCredential(Client client, String password) {
        byte[] saltBytes = PasswordUtil.randomSalt();
        String saltString = PasswordUtil.encodeBase64(saltBytes);
        String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);
        Credential credential = Credential.builder()
                .createDateTime(LocalDateTime.now(ZoneOffset.UTC))
                .password(encodedPassword)
                .salt(saltString)
                .client(client)
                .build();
        credential = credentialRepository.save(credential);
        client.setCredential(credential);
        clientRepository.save(client);
    }


}
