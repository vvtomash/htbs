package org.acme.fintech.model;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(generator = "client")
    @SequenceGenerator(name = "client", sequenceName = "client_id_seq", allocationSize = 1, initialValue = 10)
    private BigInteger id;

    @Column
    private String phone;

    @Column
    private LocalDate birthdate;

    @Column
    private String contract;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }
}
