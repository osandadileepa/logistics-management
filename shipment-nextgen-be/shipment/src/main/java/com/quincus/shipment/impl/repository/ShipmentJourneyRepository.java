package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentJourneyRepository extends JpaRepository<ShipmentJourneyEntity, String> {
    @Query(value = """
            SELECT journey
            FROM ShipmentJourneyEntity journey
              LEFT JOIN ShipmentEntity shp ON shp.shipmentJourneyId = journey.id
            WHERE shp.organization.id = :organizationId
                AND shp.orderId = :orderId
            """)
    List<ShipmentJourneyEntity> findByOrderId(String orderId, String organizationId);

}
