package org.acme.fintech.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "signup")
public class SignUp {
    @Id
    @GeneratedValue(generator = "signup")
    @SequenceGenerator(name = "signup", sequenceName = "signup_id_seq")
    private long id;

    @Version
    private long version;

    @Column(name = "otp_token", nullable = false)
    private String otpToken;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "create_timestamp")
    private LocalDateTime createDateTime;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Credential credential;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Override
    public String toString() {
        return "SignUp{" +
                "id=" + id +
                ", version=" + version +
                ", otpToken='" + otpToken + '\'' +
                '}';
    }
}
