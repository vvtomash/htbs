package org.acme.fintech.repository;

import org.acme.fintech.model.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ClientRepository extends CrudRepository<Client, Integer> {
    public Client findByPhone(String phone);
    public Client findByPhoneAndContractAndBirthdate(String phone, String contract, LocalDate birthdate);
}
