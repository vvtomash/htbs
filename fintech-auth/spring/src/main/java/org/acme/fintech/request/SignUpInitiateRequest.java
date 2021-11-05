package org.acme.fintech.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SignUpInitiateRequest {
    private String phone;
    private String password;
    private String contract;
    private LocalDate birthdate;
}
