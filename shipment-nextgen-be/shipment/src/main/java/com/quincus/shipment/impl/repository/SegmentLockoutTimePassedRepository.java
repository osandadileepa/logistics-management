package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.SegmentLockoutTimePassedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentLockoutTimePassedRepository extends JpaRepository<SegmentLockoutTimePassedEntity, String> {

    @Modifying
    @Query("DELETE FROM SegmentLockoutTimePassedEntity s WHERE s.segmentId = :segmentId")
    void deleteBySegmentId(String segmentId);
}
