package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

}
