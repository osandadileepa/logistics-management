package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructionRepository extends JpaRepository<InstructionEntity, String> {

    @Query(value = """
             SELECT
                instruction
             FROM InstructionEntity instruction
             WHERE instruction.packageJourneySegmentId in (:segmentIds)
             and instruction.organizationId = :organizationId
            """)
    List<InstructionEntity> findAllBySegmentIds(List<String> segmentIds, String organizationId);

    @Query(value = """
             SELECT
                instruction
             FROM InstructionEntity instruction
             WHERE instruction.orderId = :orderId
             and instruction.organizationId = :organizationId
            """)
    List<InstructionEntity> findAllByOrderId(String orderId, String organizationId);
}
