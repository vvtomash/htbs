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
@Table(name = "signup")
public class SignUp {
    @Id
    @GeneratedValue(generator = "signup")
    @SequenceGenerator(name = "signup", sequenceName = "signup_id_seq")
    private long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "credential_id")
    private Credential credential;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "otptoken_id")
    private OtpToken otpToken;

    @Override
    public String toString() {
        return "SignUp{" + "id=" + id + '}';
    }
}
