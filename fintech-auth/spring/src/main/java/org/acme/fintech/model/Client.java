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
    private Integer id;

    @Column(unique = true)
    private String phone;

    @Column
    private String contract;

    @Column
    private LocalDate birthdate;
}
