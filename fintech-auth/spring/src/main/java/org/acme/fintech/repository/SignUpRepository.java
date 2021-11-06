package org.acme.fintech.repository;

import org.acme.fintech.model.Device;
import org.acme.fintech.model.SignUp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignUpRepository extends CrudRepository<SignUp, Long> {
}
