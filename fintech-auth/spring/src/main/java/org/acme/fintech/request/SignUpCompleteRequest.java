package org.acme.fintech.request;

import lombok.Data;

@Data
public class SignUpCompleteRequest {
    private String phone;
    private String otpToken;
    private String pushToken;
    private String publicKey;
}
