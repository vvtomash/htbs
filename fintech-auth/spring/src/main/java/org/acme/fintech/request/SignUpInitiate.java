package org.acme.fintech.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpInitiate {
    private String phone;
    private String password;
    private String contract;
    private LocalDate birthdate;
}
