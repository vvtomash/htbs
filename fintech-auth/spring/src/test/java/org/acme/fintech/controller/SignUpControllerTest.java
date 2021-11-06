package org.acme.fintech.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SignUpControllerTest extends AbstractController {
    @Autowired
    MockMvc mockMvc;

//    @Autowired
//    SignUpRepository signUpRepository;
//
//    @Before
//    public void cleanup() {
//        signUpRepository.deleteAll();
//        clientRepository.deleteAll();
//    }

    @Test
    public void initiate() throws Exception {
//        setupClient();
//
//        SignUpInitiate request = SignUpInitiate.builder()
//                .phone("P1")
//                .password("password")
//                .contract("C1")
//                .birthdate(LocalDate.now())
//                .build();
//
//        mockMvc.perform(post("/auth/signup/initiate")
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk());
    }
}