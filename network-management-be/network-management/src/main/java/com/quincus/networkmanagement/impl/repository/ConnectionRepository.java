package com.quincus.networkmanagement.impl.repository;

import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, String>, JpaSpecificationExecutor<ConnectionEntity> {
    @Query(value = """
            SELECT COUNT(connection) > 0
            FROM ConnectionEntity connection
            WHERE connection.connectionCode = :connectionCode
            """)
    boolean isExistingConnectionCode(String connectionCode);

    Optional<ConnectionEntity> findByConnectionCode(String connectionCode);

    @Override
    @Query("SELECT c FROM ConnectionEntity c WHERE c.id = :id")
    Optional<ConnectionEntity> findById(String id);

    @Query("SELECT c FROM ConnectionEntity c WHERE c.organizationId = :organizationId AND c.active = true")
    List<ConnectionEntity> findAllActiveByOrganizationId(String organizationId);
}
