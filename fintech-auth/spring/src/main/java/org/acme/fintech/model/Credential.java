package org.acme.fintech.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credential")
public class Credential {
    @Id
    @GeneratedValue(generator = "credential")
    @SequenceGenerator(name = "credential", sequenceName = "credential_id_seq")
    private Integer id;

    @Column(length = 4000)
    private String password;

    @Column(length = 4000)
    private String salt;

    @Column
    private boolean isActive;

    @OneToOne
    private Client client;
}
