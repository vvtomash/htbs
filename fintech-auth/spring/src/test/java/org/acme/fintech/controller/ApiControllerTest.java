package org.acme.fintech.controller;

import org.acme.fintech.model.Client;
import org.acme.fintech.request.VerifyPassword;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ApiControllerTest extends AbstractController {
    @Autowired
    MockMvc mockMvc;


    @Before
    public void cleanup() {
        credentialRepository.deleteAll();
        clientRepository.deleteAll();
    }


    @Test
    public void verify_pass() throws Exception {
        Client client = setupClient();
        setupCredential(client, "password");

        VerifyPassword request = new VerifyPassword();
        request.setPhone(client.getPhone());
        request.setPassword("password");

        mockMvc.perform(post("/api/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void verify_fail() throws Exception {
        Client client = setupClient();
        setupCredential(client, "password");

        VerifyPassword request = new VerifyPassword();
        request.setPhone(client.getPhone());
        request.setPassword("wrongPassword");
        mockMvc.perform(post("/api/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}