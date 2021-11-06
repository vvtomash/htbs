package org.acme.fintech.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpComplete {
    private String phone;
    private String otpToken;
    private String pushToken;
    private String publicKey;
}
