package com.quincus.networkmanagement.impl.repository;

import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<NodeEntity, String>, JpaSpecificationExecutor<NodeEntity> {
    @Query(value = """
            SELECT COUNT(node) > 0
            FROM NodeEntity node
            WHERE node.nodeCode = :code
            """)
    boolean isExistingNodeCode(String code);

    Optional<NodeEntity> findByNodeCode(String nodeCode);

    @Override
    @Query("SELECT n FROM NodeEntity n WHERE n.id = :id")
    Optional<NodeEntity> findById(String id);

    @Query("SELECT n FROM NodeEntity n WHERE n.organizationId = :organizationId AND n.active = true")
    List<NodeEntity> findAllActiveByOrganizationId(String organizationId);
}
