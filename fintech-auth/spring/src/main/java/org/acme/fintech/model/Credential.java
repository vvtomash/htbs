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
@Table(name = "credential")
public class Credential {
    @Id
    @GeneratedValue(generator = "credential")
    @SequenceGenerator(name = "credential", sequenceName = "credential_id_seq")
    private long id;

    @Version
    private long version;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String salt;

    @Column(name = "create_timestamp", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "activate_timestamp")
    private LocalDateTime activateDateTime;

    @Column(name = "is_active")
    private boolean isActive;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;
}
