package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {
    @Query("SELECT c FROM CustomerEntity c " +
            "WHERE c.code = :code " +
            "AND c.organizationId = :organizationId")
    Optional<CustomerEntity> findByCodeAndOrganizationId(String code, String organizationId);

    List<CustomerEntity> findByOrganizationId(String organizationId);

    @Query("SELECT c FROM CustomerEntity c WHERE c.organizationId = :organizationId and (:name is null or LOWER(c.name) LIKE LOWER(concat('%', concat(:name, '%'))))")
    Page<CustomerEntity> findByOrganizationIdAndNameContaining(String organizationId, String name, Pageable pageable);

    Page<CustomerEntity> findByOrganizationId(String organizationId, Pageable pageable);
}
