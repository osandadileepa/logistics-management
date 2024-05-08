package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentPostProcessServiceTest {

    @InjectMocks
    ShipmentPostProcessService shipmentPostProcessService;
    @Mock
    MessageApi messageApi;

    private static PackageJourneySegment createDummyGroundSegment() {
        PackageJourneySegment segment = createDummySegment();
        segment.setTransportType(TransportType.GROUND);
        OffsetDateTime pickupTime = OffsetDateTime.now(Clock.systemUTC());
        OffsetDateTime dropOffTime = pickupTime.plusDays(3);
        segment.setPickUpTime(pickupTime.toString());
        segment.setDropOffTime(dropOffTime.toString());
        return segment;
    }

    private static PackageJourneySegment createDummyAirSegment() {
        PackageJourneySegment segment = createDummySegment();
        segment.setTransportType(TransportType.AIR);
        OffsetDateTime departureTime = OffsetDateTime.now(Clock.systemUTC());
        segment.setDepartureTime(departureTime.toString());
        segment.setAirlineCode("AB");
        segment.setFlightNumber("123");
        return segment;
    }

    private static PackageJourneySegment createDummySegment() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Partner partner = new Partner();
        segment.setPartner(partner);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        return segment;
    }

    @Test
    void givenJourneyHasNoHardConstraintWhenSendUpdateToOtherProductsThenSendShipmentToQShip() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setAlerts(List.of(new Alert("This is a soft constraint", AlertType.WARNING)));
        PackageJourneySegment segment = createDummyAirSegment();
        shipmentJourney.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdateToQship(shipment);

        verify(messageApi, times(1)).sendShipmentToQShip(any(Shipment.class));
    }

    @Test
    void givenJourneyHasHardConstraintWhenSendUpdateToOtherProductsThenSendShipmentToQShip() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setAlerts(List.of(new Alert("This is a hard constraint", AlertType.ERROR)));
        PackageJourneySegment segment = createDummyAirSegment();
        shipmentJourney.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdateToQship(shipment);

        verify(messageApi, times(1)).sendShipmentToQShip(shipment);
    }

    @Test
    void givenSegmentHasNoHardConstraintWhenSendUpdateToOtherProductsThenSendShipmentToQShip() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment segment = createDummyGroundSegment();
        segment.setAlerts(List.of(new Alert("This is a soft constraint", AlertType.WARNING)));
        shipmentJourney.setPackageJourneySegments(List.of(segment));
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdateToQship(shipment);

        verify(messageApi, times(1)).sendShipmentToQShip(any(Shipment.class));
    }

    @Test
    void givenSegmentHasHardConstraintWhenSendUpdateToOtherProductsThenSendShipmentToQShip() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment segment = createDummyGroundSegment();
        segment.setAlerts(List.of(new Alert("This is a hard constraint", AlertType.ERROR)));
        shipmentJourney.setPackageJourneySegments(List.of(segment));
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdateToQship(shipment);

        verify(messageApi, times(1)).sendShipmentToQShip(any(Shipment.class));
    }

    @Test
    void givenOtherJourneyHasNoHardConstraintWhenSendUpdateToOtherProductsThenSendShipmentToQShip() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setAlerts(List.of(new Alert("This is a soft constraint", AlertType.WARNING)));
        PackageJourneySegment segment = createDummyGroundSegment();
        shipmentJourney.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdateToQship(shipment, shipmentJourney);

        verify(messageApi, times(1)).sendShipmentWithJourneyToQShip(any(Shipment.class), any(ShipmentJourney.class));
    }

    @Test
    void sendSingleSegmentToQship_segmentIdArgument_shouldExecute() {
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment segment = createDummyAirSegment();
        shipmentJourney.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(shipmentJourney);
        String segmentId = "segment-id1";

        shipmentPostProcessService.sendSingleSegmentToQship(shipment, segmentId);

        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(shipment, segmentId);
    }

    @Test
    void sendSingleSegmentToQship_segmentObjectArgument_shouldExecute() {
        Shipment shipment = new Shipment();
        PackageJourneySegment segment = createDummyAirSegment();

        shipmentPostProcessService.sendSingleSegmentToQship(shipment, segment);

        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(shipment, segment);
    }

    @Test
    void sendUpdatedSegmentToDispatch_validArguments_shouldExecute() {
        Milestone milestone = new Milestone();
        milestone.setSegmentUpdatedFromMilestone(true);
        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment segment = createDummyAirSegment();
        shipmentJourney.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(shipmentJourney);

        shipmentPostProcessService.sendUpdatedSegmentToDispatch(milestone, shipment);

        verify(messageApi, times(1)).sendSegmentDispatch(shipment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void sendUpdatedSegmentToDispatch_segmentNotUpdated_shouldNotExecute() {
        Milestone milestone = new Milestone();
        milestone.setSegmentUpdatedFromMilestone(false);
        Shipment shipment = new Shipment();

        shipmentPostProcessService.sendUpdatedSegmentToDispatch(milestone, shipment);

        verify(messageApi, never()).sendSegmentDispatch(any(), any(SegmentDispatchType.class), eq(DspSegmentMsgUpdateSource.CLIENT));
    }
}
