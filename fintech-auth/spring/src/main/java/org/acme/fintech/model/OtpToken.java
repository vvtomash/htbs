package org.acme.fintech.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otptoken")
public class OtpToken {
    @Id
    @GeneratedValue(generator = "otptoken")
    @SequenceGenerator(name = "otptoken", sequenceName = "otptoken_id_seq")
    private long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "create_timestamp", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "expiry_timestamp", nullable = false)
    private LocalDateTime expiryDateTime;

    @Override
    public String toString() {
        return "OtpToken{" +
                "id=" + id +
                ", code='" + code + '\'' +
                '}';
    }
}
