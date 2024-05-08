package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, String> {

    @Query("SELECT c FROM ServiceTypeEntity c WHERE c.organizationId = :organizationId and c.code = :code")
    Optional<ServiceTypeEntity> findByCodeAndOrganizationId(String code, String organizationId);

    @Query("SELECT c FROM ServiceTypeEntity c WHERE c.organizationId = :organizationId and (:name is null or LOWER(c.name) LIKE LOWER(concat('%', concat(:name, '%'))))")
    Page<ServiceTypeEntity> findByOrganizationIdAndNameContaining(String organizationId, String name, Pageable pageable);

    Page<ServiceTypeEntity> findByOrganizationId(String organizationId, Pageable pageable);

    @Query("SELECT s FROM ServiceTypeEntity s " +
            "WHERE EXISTS (SELECT 1 FROM NetworkLaneEntity n WHERE n.serviceTypeId = s.id) " +
            "AND s.organizationId = :organizationId and (:name is null or LOWER(s.name) LIKE LOWER(concat('%', concat(:name, '%'))))")
    Page<ServiceTypeEntity> findByOrganizationIdAndNameContainingForNetworkLane(String organizationId, String name, Pageable pageable);

    @Query("SELECT s FROM ServiceTypeEntity s " +
            "WHERE EXISTS (SELECT 1 FROM NetworkLaneEntity n WHERE n.serviceTypeId = s.id) " +
            "AND s.organizationId = :organizationId")
    Page<ServiceTypeEntity> findByOrganizationIdForNetworkLane(String organizationId, Pageable pageable);
}
