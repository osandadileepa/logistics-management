package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<ArchivedEntity, String> {
    Optional<ArchivedEntity> findByReferenceIdAndOrganizationId(String referenceId, String organizationId);

    Optional<ArchivedEntity> findFirstByReferenceIdAndOrganizationIdOrderByCreateTimeDesc(String referenceId, String organizationId);
}
