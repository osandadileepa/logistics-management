package com.quincus.shipment.impl.service.scheduler;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentLockoutTimeSchedulerTest {
    @InjectMocks
    private ShipmentLockoutTimeScheduler shipmentLockoutTimeScheduler;

    @Mock
    private MessageApi messageApi;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private PackageJourneySegmentService segmentService;

    @Test
    void checkLockoutTimeMissed_shipmentsFound_shouldCallSendToDispatch() {
        Shipment shipment = new Shipment();
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setRefId("1");
        airSegment.setSequence("1");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journeyEntity = new ShipmentJourney();
        journeyEntity.setPackageJourneySegments(new ArrayList<>());
        journeyEntity.addPackageJourneySegment(airSegment);
        shipment.setShipmentJourney(journeyEntity);

        when(shipmentService.findActiveShipmentsWithAirSegment()).thenReturn(List.of(shipment));
        when(segmentService.isSegmentLockoutTimeMissed(airSegment)).thenReturn(true);

        shipmentLockoutTimeScheduler.checkLockoutTimeMissed();

        verify(messageApi, times(1))
                .sendDispatchCanceledFlight(any(), eq(airSegment.getSegmentId()), eq("flight canceled"));
    }

    @Test
    void checkLockoutTimeMissed_noShipmentsFound_shouldNotCallSendToDispatch() {
        when(shipmentService.findActiveShipmentsWithAirSegment()).thenReturn(Collections.emptyList());

        shipmentLockoutTimeScheduler.checkLockoutTimeMissed();

        verify(messageApi, never()).sendDispatchCanceledFlight(any(), anyString(), anyString());
    }

    @Test
    void getAirSegmentLockoutTimeMissed_validScenario_shouldReturnSegment() {
        Shipment shipment = new Shipment();
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(airSegment));
        shipment.setShipmentJourney(journey);

        when(segmentService.isSegmentLockoutTimeMissed(airSegment)).thenReturn(true);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNotNull();
        assertThat(result.getSegmentId()).isEqualTo(airSegment.getSegmentId());
    }

    @Test
    void getAirSegmentLockoutTimeMissed_nonAirSegment_shouldReturnNull() {
        Shipment shipment = new Shipment();
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("groundSegmentId");
        airSegment.setTransportType(TransportType.GROUND);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(airSegment));
        shipment.setShipmentJourney(journey);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNull();
    }

    @Test
    void getAirSegmentLockoutTimeMissed_airSegmentInProgress_shouldReturnNull() {
        Shipment shipment = new Shipment();
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.IN_PROGRESS);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(airSegment));
        shipment.setShipmentJourney(journey);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNull();
    }

    @Test
    void getAirSegmentLockoutTimeMissed_previousSegmentInProgress_shouldReturnSegment() {
        Shipment shipment = new Shipment();
        PackageJourneySegment groundSegment = new PackageJourneySegment();
        groundSegment.setSegmentId("groundSegmentId");
        groundSegment.setRefId("1");
        groundSegment.setSequence("1");
        groundSegment.setTransportType(TransportType.GROUND);
        groundSegment.setStatus(SegmentStatus.IN_PROGRESS);
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setRefId("2");
        airSegment.setSequence("2");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(groundSegment, airSegment));
        shipment.setShipmentJourney(journey);

        when(segmentService.isSegmentLockoutTimeMissed(airSegment)).thenReturn(true);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNotNull();
        assertThat(result.getSegmentId()).isEqualTo(airSegment.getSegmentId());
    }

    @Test
    void getAirSegmentLockoutTimeMissed_previousSegmentCompleted_shouldReturnSegment() {
        Shipment shipment = new Shipment();
        PackageJourneySegment groundSegment = new PackageJourneySegment();
        groundSegment.setSegmentId("groundSegmentId");
        groundSegment.setRefId("1");
        groundSegment.setSequence("1");
        groundSegment.setTransportType(TransportType.GROUND);
        groundSegment.setStatus(SegmentStatus.COMPLETED);
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setRefId("2");
        airSegment.setSequence("2");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(groundSegment, airSegment));
        shipment.setShipmentJourney(journey);

        when(segmentService.isSegmentLockoutTimeMissed(airSegment)).thenReturn(true);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNotNull();
        assertThat(result.getSegmentId()).isEqualTo(airSegment.getSegmentId());
    }

    @Test
    void getAirSegmentLockoutTimeMissed_previousSegmentPlanned_shouldReturnNull() {
        Shipment shipment = new Shipment();
        PackageJourneySegment groundSegment = new PackageJourneySegment();
        groundSegment.setSegmentId("groundSegmentId");
        groundSegment.setRefId("1");
        groundSegment.setSequence("1");
        groundSegment.setTransportType(TransportType.GROUND);
        groundSegment.setStatus(SegmentStatus.PLANNED);
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setRefId("2");
        airSegment.setSequence("2");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(groundSegment, airSegment));
        shipment.setShipmentJourney(journey);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNull();
    }

    @Test
    void getAirSegmentLockoutTimeMissed_lockoutTimeMissed_shouldReturnNull() {
        Shipment shipment = new Shipment();
        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setSegmentId("airSegmentId");
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setStatus(SegmentStatus.PLANNED);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(airSegment));
        shipment.setShipmentJourney(journey);

        when(segmentService.isSegmentLockoutTimeMissed(airSegment)).thenReturn(false);

        PackageJourneySegment result = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);

        assertThat(result).isNull();
    }
}
