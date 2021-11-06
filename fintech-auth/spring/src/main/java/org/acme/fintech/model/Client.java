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

    @Version
    private long version;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String contract;

    @Column(nullable = false)
    private LocalDate birthdate;

    @OneToOne(fetch = FetchType.LAZY)
    private Credential credential;

    @OneToOne(fetch = FetchType.LAZY)
    private SignUp signUp;

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", version=" + version +
                ", phone='" + phone + '\'' +
                '}';
    }
}
