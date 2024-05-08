package com.quincus.shipment.impl.repository;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, String> {

    @Query(value = "SELECT m.milestoneCode AS milestoneCode, m.milestoneTime AS milestoneTime " +
            "FROM MilestoneEntity m " +
            "WHERE m.shipment.id = :shipmentId")
    Page<Tuple> findByShipmentId(String shipmentId, Pageable pageable);


    @Query(nativeQuery = true,
            value = "SELECT ms.id, ms.shipment_id, ms.milestone_name FROM milestone ms " +
                    "INNER JOIN ( SELECT shipment_id, MAX(CAST(milestone_time AS DATETIME)) AS max_date FROM milestone WHERE " +
                    "shipment_id in (:shipmentIds) " +
                    "GROUP BY shipment_id) tempTable ON ms.shipment_id = tempTable.shipment_id " +
                    "AND CAST(ms.milestone_time AS DATETIME) = tempTable.max_date order by ms.create_time desc")
    List<Object[]> findRecentMilestoneByShipmentIds(List<String> shipmentIds);

    Optional<MilestoneEntity> findByMilestoneCodeAndShipmentIdAndSegment(MilestoneCode milestoneCode, String shipmentId, PackageJourneySegmentEntity segment);

    Optional<MilestoneEntity> findByMilestoneCodeAndShipmentIdAndSegmentId(MilestoneCode milestoneCode, String shipmentId, String segmentId);

    @Query(value = """
            SELECT CASE
              WHEN (SELECT COUNT(*) FROM milestone m
                      LEFT JOIN shipment shp ON m.shipment_id = shp.id
                    WHERE shp.order_id = :orderId
                      AND m.code = 'OM_BOOKED'
                      AND shp.status != 'CANCELLED'
                      AND shp.deleted != true) =
                   (SELECT COUNT(*) FROM milestone m
                      LEFT JOIN shipment shp ON m.shipment_id = shp.id
                    WHERE shp.order_id = :orderId
                      AND shp.status != 'CANCELLED'
                      AND shp.deleted != true
                      AND m.code = :milestoneCode
                      AND m.segment_id = :segmentId)
              THEN true ELSE false END
            """, nativeQuery = true)
    int isAllShipmentFromOrderSameMilestone(String orderId, String segmentId, String milestoneCode);

    @Query(value = """
            WITH order_info AS (
              SELECT order_id FROM shipment WHERE id = :shipmentId
            )
            SELECT CASE
              WHEN (SELECT COUNT(*) FROM milestone m
                      LEFT JOIN shipment shp ON m.shipment_id = shp.id
                    WHERE shp.order_id = (SELECT order_id FROM order_info)
                      AND m.code = 'OM_BOOKED'
                      AND shp.status != 'CANCELLED'
                      AND shp.deleted != true) =
                   (SELECT COUNT(*) FROM milestone m
                      LEFT JOIN shipment shp ON m.shipment_id = shp.id
                    WHERE shp.order_id = (SELECT order_id FROM order_info)
                      AND shp.status != 'CANCELLED'
                      AND shp.deleted != true
                      AND m.code = :milestoneCode
                      AND m.segment_id = :segmentId)
              THEN true ELSE false END
            """, nativeQuery = true)
    int isAllRelatedShipmentSameMilestone(String shipmentId, String segmentId, String milestoneCode);

    //TODO: remove 'deleted' in WHERE clause once confirmed by Su the behavior for missing shipments
    @Query("""
            SELECT m
            FROM MilestoneEntity m
              LEFT JOIN ShipmentEntity shp ON m.shipmentId = shp.id
              LEFT JOIN PackageJourneySegmentEntity seg ON m.segmentId = seg.id
            WHERE shp.deleted != true
              AND shp.status != 'CANCELLED'
              AND m.segmentId IS NOT NULL
              AND m.shipmentId IS NOT NULL
              AND (seg.status = 'PLANNED' OR seg.status = 'IN_PROGRESS')
              AND seg.transportType = 'GROUND'
              AND m.shipmentId NOT IN (:missingShipmentIds)
             """)
    List<MilestoneEntity> getMilestonesWithLikelyPendingSegmentUpdate(List<String> missingShipmentIds);
}
