package org.acme.fintech.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInComplete {
    private String phone;
    private String otpCode;
}
