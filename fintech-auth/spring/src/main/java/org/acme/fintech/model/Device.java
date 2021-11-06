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
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(generator = "device")
    @SequenceGenerator(name = "device", sequenceName = "device_id_seq")
    private long id;

    @Version
    private long version;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "push_token", columnDefinition = "TEXT")
    private String pushToken;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "activate_timestamp", nullable = false)
    private LocalDateTime activateDateTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

}
