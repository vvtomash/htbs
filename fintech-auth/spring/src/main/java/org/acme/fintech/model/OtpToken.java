package org.acme.fintech.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class OtpToken {
    private String code;
    private LocalDateTime expTime;
}
