package org.acme.fintech;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@EntityScan
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @PostConstruct
    public void init() {
        // Cleanup
        credentialRepository.deleteAll();
        clientRepository.deleteAll();

        // Seed client
        Client client = Client.builder()
                .phone("380662651598")
                .contract("C1")
                .birthdate(LocalDate.of(2020, 1, 2))
                .build();
        client = clientRepository.save(client);

        // Seed credential
        String rawPassword = "lysakaleksey";
        byte[] saltBytes = PasswordUtil.randomSalt();
        String saltString = PasswordUtil.encodeBase64(saltBytes);
        String encodedPassword = PasswordUtil.encodePassword(rawPassword, saltBytes);
        Credential credential = Credential.builder()
                .password(encodedPassword)
                .salt(saltString)
                .client(client)
                .isActive(true)
                .build();
        credentialRepository.save(credential);
    }
}
