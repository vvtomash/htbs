package org.acme.fintech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.fintech.model.Client;
import org.acme.fintech.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public abstract class AbstractController {
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ClientRepository clientRepository;

    protected Client setupClient() {
        Client client = Client.builder()
                .phone("P1")
                .contract("C1")
                .birthdate(LocalDate.now())
                .build();
        return clientRepository.save(client);
    }

}
