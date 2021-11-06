package org.acme.fintech.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(generator = "client")
    @SequenceGenerator(name = "client", sequenceName = "client_id_seq")
    private long id;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String contract;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.INACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id")
    private Credential credential;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signup_id")
    private SignUp signUp;

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
