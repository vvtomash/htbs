package org.acme.fintech.request;

import lombok.Data;

@Data
public class VerifyPassword {
    private String phone;
    private String password;
}
