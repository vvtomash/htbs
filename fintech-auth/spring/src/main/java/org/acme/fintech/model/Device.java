package org.acme.fintech.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(generator = "device")
    @SequenceGenerator(name = "device", sequenceName = "device_id_seq")
    private Integer id;

    @Column
    private String name;

    @Column
    private String publicKey;

    @ManyToOne
    @JoinColumn(name = "client")
    private Client client;

}
