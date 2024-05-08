package com.quincus.shipment.impl.repository;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, String> {


    @Query(value = "SELECT c FROM LocationEntity c " +
            "WHERE c.externalId = :externalId " +
            "AND c.organizationId = :organizationId ")
    Optional<LocationEntity> findByExternalIdAndOrganizationId(String externalId, String organizationId);

    List<LocationEntity> findByOrganizationIdAndExternalIdIn(String organizationId, List<String> externalIds);

    @Query(value = "  SELECT c FROM LocationEntity c" +
            "  WHERE c.organizationId = :organizationId" +
            "  AND c.type = :type" +
            "  AND (" +
            "    LOWER(c.code) LIKE LOWER(concat('%', concat(:code, '%'))) OR :code IS null" +
            "    OR LOWER(c.name) LIKE LOWER(concat('%', concat(:code, '%')))" +
            "  )")
    Page<LocationEntity> findByOrganizationIdAndTypeAndCodeAndNameContaining(LocationType type, String organizationId, String code, Pageable pageable);

    @Query("SELECT c FROM LocationEntity c WHERE c.organizationId = :organizationId and c.type = :type")
    Page<LocationEntity> findByOrganizationIdAndType(LocationType type, String organizationId, Pageable pageable);
    
}
