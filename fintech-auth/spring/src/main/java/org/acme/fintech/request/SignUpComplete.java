package org.acme.fintech.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpComplete {
    private String phone;
    private String otpCode;
    private String password;
    private String pushToken;
    private String publicKey;
    private String deviceId;
}
