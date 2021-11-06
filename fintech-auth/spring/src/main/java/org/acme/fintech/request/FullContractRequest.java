package org.acme.fintech.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullContractRequest {
    private String phone;
    private String contract;
    private LocalDate birthdate;
}
