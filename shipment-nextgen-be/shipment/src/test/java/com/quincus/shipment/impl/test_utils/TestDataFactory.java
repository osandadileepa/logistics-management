package com.quincus.shipment.impl.test_utils;

import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.NONE)
public class TestDataFactory {

    public static PackageJourneySegmentEntity createPackageJourneySegmentEntity(
            final String refId,
            final String sequence
    ) {
        final PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setRefId(refId);
        segmentEntity.setSequence(sequence);
        return segmentEntity;
    }

    public static ShipmentJourney createShipmentJourney(
            final String shipmentId,
            final String journeyId,
            final String orderId,
            final JourneyStatus journeyStatus,
            final PackageJourneySegment packageJourneySegment
    ) {
        final ShipmentJourney shipmentJourney = createShipmentJourney(shipmentId, journeyId, orderId, journeyStatus);
        shipmentJourney.addPackageJourneySegment(packageJourneySegment);
        return shipmentJourney;
    }

    public static ShipmentJourney createShipmentJourney(
            final String shipmentId,
            final String journeyId,
            final String orderId,
            final JourneyStatus journeyStatus
    ) {
        final ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setShipmentId(shipmentId);
        shipmentJourney.setJourneyId(journeyId);
        shipmentJourney.setOrderId(orderId);
        shipmentJourney.setStatus(journeyStatus);
        return shipmentJourney;
    }

    public static ShipmentEntity createShipmentEntity(
            final ShipmentJourneyEntity shipmentJourneyEntity,
            final OrganizationEntity organizationEntity,
            final String shipmentId
    ) {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        shipmentEntity.setOrganization(organizationEntity);
        shipmentEntity.setId(shipmentId);
        shipmentEntity.setShipmentTrackingId(UUID.randomUUID().toString());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId("test-order1");
        shipmentEntity.setOrder(orderEntity);
        return shipmentEntity;
    }

    public static Shipment createShipment(
            final String shipmentId,
            final ShipmentJourney shipmentJourney,
            final Order order
    ) {
        final Shipment shipment = new Shipment();
        shipment.setId(shipmentId);
        shipment.setOrder(order);
        shipment.setShipmentJourney(shipmentJourney);
        return shipment;
    }

    public static ShipmentJourneyEntity createShipmentJourneyEntity(
            final String id,
            final PackageJourneySegmentEntity packageJourneySegmentEntity
    ) {
        final ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(id);
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegmentEntity);
        return shipmentJourneyEntity;
    }

    public static PackageJourneySegment createPackageJourneySegment(
            final String segmentId,
            final String refId,
            final String sequence
    ) {
        PackageJourneySegment allowed1 = new PackageJourneySegment();
        allowed1.setSegmentId(segmentId);
        allowed1.setRefId(refId);
        allowed1.setSequence(sequence);
        return allowed1;
    }
}
