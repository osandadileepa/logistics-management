package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<PartnerEntity, String> {

    Optional<PartnerEntity> findByExternalIdAndOrganizationId(String externalId, String organizationId);

    List<PartnerEntity> findByExternalIdInAndOrganizationId(List<String> externalIds, String organizationId);

    Optional<PartnerEntity> findByNameIgnoreCaseAndOrganizationId(String name, String organizationId);
}
