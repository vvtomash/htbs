package org.acme.fintech.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDataRequest {
    private String phone;
    private String contract;
    private LocalDate birthdate;
}
