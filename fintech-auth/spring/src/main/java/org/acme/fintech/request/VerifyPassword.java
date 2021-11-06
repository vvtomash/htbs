package org.acme.fintech.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPassword {
    private String phone;
    private String password;
}
