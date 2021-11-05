package org.acme.fintech.controller;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.repository.ClientRepository;
import org.acme.fintech.repository.CredentialRepository;
import org.acme.fintech.repository.DeviceRepository;
import org.acme.fintech.request.SignUpInitiateRequest;
import org.acme.fintech.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@RestController()
@RequestMapping("/auth/signup")
public class SignUpController {
    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @PostConstruct
    public void init() {

    }

    @PostMapping("/initiate")
    public ResponseEntity<String> signUp(@RequestBody SignUpInitiateRequest request) {
        String phone = request.getPhone();
        String contract = request.getContract();
        LocalDate birthdate = request.getBirthdate();
        Client client = clientRepository.findByPhoneAndContractAndBirthdate(phone, contract, birthdate);
        if (client == null) {
            return ResponseEntity.badRequest().build();
        }

        // Generate disabled credential
        String password = request.getPassword();
        byte[] saltBytes = PasswordUtil.randomSalt();
        String saltString = PasswordUtil.encodeBase64(saltBytes);
        String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);
        Credential credential = Credential.builder()
                .password(encodedPassword)
                .salt(saltString)
                .isActive(false)
                .client(client)
                .build();
        credentialRepository.save(credential);

        // Generate otp token
        // Send otp token

        return ResponseEntity.accepted().build();
    }
}
