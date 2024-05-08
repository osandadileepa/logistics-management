package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkLaneRepository extends JpaRepository<NetworkLaneEntity, String>, JpaSpecificationExecutor<NetworkLaneEntity> {

    @Query(value = "SELECT nl FROM NetworkLaneEntity nl " +
            "WHERE (:serviceTypeCode IS NULL OR nl.serviceType.code = :serviceTypeCode)  " +
            "AND  nl.organizationId = :organizationId")
    Page<NetworkLaneEntity> findByServiceTypeCodeAndOrganizationId(String serviceTypeCode, String organizationId,
                                                                   Pageable pageable);

    @Override
    long count(Specification specs);
}