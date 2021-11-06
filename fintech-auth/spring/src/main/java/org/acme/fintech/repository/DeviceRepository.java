package org.acme.fintech.repository;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {
}
