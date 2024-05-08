package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightStatusResult;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.impl.mapper.ShipmentMessageDtoMapper;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightStatsEventPostProcessServiceTest {

    @InjectMocks
    private FlightStatsEventPostProcessService flightStatsEventPostProcessService;
    @Mock
    private MessageApi messageApi;
    @Mock
    private MilestoneService milestoneService;
    @Mock
    private NotificationApi notificationApi;
    @Mock
    private ShipmentMessageDtoMapper shipmentMessageDtoMapper;
    @Mock
    private AlertService alertService;

    @Test
    void sendUpdatedSegmentToQShip_withUpdatedSegmentAndListOfShipmentDto_verifyCallToMessageApiSendUpdatedSegmentToQShip() {
        // Given
        PackageJourneySegmentEntity updatedSegmentEntity = new PackageJourneySegmentEntity();
        updatedSegmentEntity.setRefId("1");
        updatedSegmentEntity.setSequence("1");

        List<ShipmentEntity> relatedShipments = new ArrayList<>();
        relatedShipments.add(new ShipmentEntity());
        relatedShipments.add(new ShipmentEntity());

        when(shipmentMessageDtoMapper.mapAllToDto(relatedShipments)).thenReturn(List.of(createDummyShipmentDto(), createDummyShipmentDto()));

        // When
        flightStatsEventPostProcessService.sendUpdatedSegmentToQShip(updatedSegmentEntity, relatedShipments);

        // Then
        // Verify that messageApi.sendUpdatedSegmentToQShip() is called for each shipment
        verify(messageApi, times(2)).sendUpdatedSegmentFromShipment(any(ShipmentMessageDto.class), any());
    }

    @Test
    void processAlertsAndSendCancellationMessages_withValidSegmentIdAndAlertList_shouldSendMessagesForEachShipmentAndTriggerAlertServiceToSaveAlerts() {
        // Given
        FlightStatusResult result = new FlightStatusResult();
        result.addSegmentId("segmentId1");
        result.addSegmentId("segmentId2");

        result.addAlert(new Alert());
        result.addAlert(new Alert());

        // When
        flightStatsEventPostProcessService.processAlerts(result);

        // Then
        verify(alertService, times(1)).saveAll(result.getAlerts());
    }

    @Test
    void processSingleFlightStat_withFlightEventOccurred_shouldProcessFlightStat() {
        // Given
        ShipmentEntity shipment1 = createDummyShipmentEntities("segment1");
        ShipmentEntity shipment2 = createDummyShipmentEntities("segment1");

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segment1");

        FlightStatus flightStatus = new FlightStatus();
        flightStatus.setEventName(FlightEventName.FLIGHT_DEPARTED);

        Flight flight = new Flight();
        flight.setFlightStatus(flightStatus);

        Milestone milestoneForShipment2 = new Milestone();
        when(milestoneService.createMilestoneFromFlightEvent(eq(createDummyShipmentDto()), any(), eq(flight))).thenReturn(milestoneForShipment2);

        when(shipmentMessageDtoMapper.mapToDto(any(ShipmentEntity.class))).thenReturn(createDummyShipmentDto());

        when(shipmentMessageDtoMapper.mapToShipment(any(ShipmentMessageDto.class))).thenReturn(createDummyShipment(UUID.randomUUID().toString()));

        // When
        flightStatsEventPostProcessService.processFlightStats(List.of(shipment1, shipment2), segmentEntity, flight);

        // Then
        verify(messageApi, times(2)).sendUpdatedSegmentFromShipment(any(ShipmentMessageDto.class), any());

        // Verify that messageApi.sendFlightMilestoneMessage() is called with the correct arguments
        verify(messageApi, times(2)).sendFlightMilestoneMessage(any(), any(), any(), any());

        // Verify that notificationApi.sendNotification() is called with the correct arguments
        verify(notificationApi, times(2)).sendNotification(any());
    }

    @Test
    void processSingleFlightStat_withFlightEventNotOccurred_shouldNotProcessFlightStat() {
        // Given
        ShipmentEntity shipment1 = createDummyShipmentEntities("segment1");
        ShipmentEntity shipment2 = createDummyShipmentEntities("segment1");
        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setId("segment1");

        FlightStatus flightStatus = new FlightStatus();
        flightStatus.setEventName(FlightEventName.CANCELLED);

        Flight flight = new Flight();
        flight.setFlightStatus(flightStatus);

        // When
        flightStatsEventPostProcessService.processFlightStats(List.of(shipment1, shipment2), segmentEntity, flight);

        // no interaction base on flight status event name
        verify(messageApi, times(0)).sendFlightMilestoneMessage(any(), any(), any(), any());
        verifyNoInteractions(notificationApi);

    }

    private Shipment createDummyShipment(String segmentId) {
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setSegmentId(segmentId);

        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.addPackageJourneySegment(packageJourneySegment);

        Organization organization = new Organization();
        organization.setId("shpv2");

        Shipment shipment = new Shipment();
        shipment.setShipmentJourney(shipmentJourney);
        shipment.setOrganization(organization);
        return shipment;
    }

    private ShipmentMessageDto createDummyShipmentDto() {
        Organization organization = new Organization();
        organization.setId("shpv2");

        ShipmentMessageDto shipment = new ShipmentMessageDto();
        shipment.setOrganizationId(organization.getId());
        return shipment;
    }

    private ShipmentEntity createDummyShipmentEntities(String segmentId) {
        PackageJourneySegmentEntity packageJourneySegment = new PackageJourneySegmentEntity();
        packageJourneySegment.setId(segmentId);

        ShipmentJourneyEntity shipmentJourney = new ShipmentJourneyEntity();
        shipmentJourney.addPackageJourneySegment(packageJourneySegment);

        OrganizationEntity organization = new OrganizationEntity();
        organization.setId("shpv2");

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setShipmentJourney(shipmentJourney);
        shipment.setOrganization(organization);
        return shipment;
    }
}
