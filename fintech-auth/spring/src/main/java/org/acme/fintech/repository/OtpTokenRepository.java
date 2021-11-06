package org.acme.fintech.repository;

import org.acme.fintech.model.OtpToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpTokenRepository extends CrudRepository<OtpToken, Long> {
}
