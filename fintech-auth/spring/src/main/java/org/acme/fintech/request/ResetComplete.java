package org.acme.fintech.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetComplete {
    private String phone;
    private String otpCode;
    private String password;
}
