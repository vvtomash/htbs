package org.acme.fintech.repository;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends CrudRepository<Credential, Integer> {
    public Credential findByClient(Client client);
}
