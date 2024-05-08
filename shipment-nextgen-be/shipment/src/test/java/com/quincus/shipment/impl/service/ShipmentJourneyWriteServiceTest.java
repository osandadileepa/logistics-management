package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.repository.ShipmentJourneyRepository;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.quincus.shipment.api.constant.AlertMessage.FAILED_PICKUP_OR_DELIVERY;
import static com.quincus.shipment.api.constant.AlertMessage.MISSING_MANDATORY_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyWriteServiceTest {

    private static final String GIVEN_JOURNEY_ID = "SHP-JOURNEY-ID";
    private static final String SEGMENT_NO = " [Segment %d]";
    @InjectMocks
    private ShipmentJourneyWriteService shipmentJourneyWriteService;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ShipmentJourneyRepository shipmentJourneyRepository;
    @Mock
    private FlightStatsEventService flightStatsEventService;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private SegmentUpdateChecker segmentUpdateChecker;
    @Mock
    private SegmentReferenceProvider segmentReferenceProvider;

    @Test
    void givenCorrectDataWithoutSegments_whenUpdateShipmentJourneyAndUpdateSegments_thenSaveSuccessfully() {
        ShipmentJourney shipmentJourneyUpdates = new ShipmentJourney();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        OrderEntity orderEntity = new OrderEntity();
        ShipmentJourney previousShipmentJourney = new ShipmentJourney();
        previousShipmentJourney.setJourneyId(GIVEN_JOURNEY_ID);

        PackageJourneySegmentEntity segmentEntity = new PackageJourneySegmentEntity();
        segmentEntity.setRefId("0");
        segmentEntity.setSequence("0");
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity);

        when(shipmentJourneyRepository.save(any(ShipmentJourneyEntity.class)))
                .thenReturn(shipmentJourneyEntity);

        // When
        ShipmentJourney result = shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(
                shipmentJourneyUpdates,
                shipmentJourneyEntity,
                orderEntity
        );

        assertThat(result).isNotNull();
        assertThat(result.getJourneyId()).isEqualTo(GIVEN_JOURNEY_ID);
        verify(shipmentJourneyRepository, times(1)).save(any());
        verifyNoInteractions(flightStatsEventService);
    }

    @Test
    void givenCorrectData_whenUpdateShipmentJourneyAndUpdateSegments_thenSaveSuccessfully() {
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("0");
        segment1.setSequence("0");
        Instruction newInstruction = new Instruction();
        newInstruction.setExternalId("ext-instruction-1");
        newInstruction.setSource("segment");
        newInstruction.setLabel("Segment Y Instruction");
        newInstruction.setValue("Instruction Value Z");
        segment1.setInstructions(new ArrayList<>(List.of(newInstruction)));
        ShipmentJourney shipmentJourneyUpdates = new ShipmentJourney();
        shipmentJourneyUpdates.addPackageJourneySegment(segment1);
        shipmentJourneyUpdates.setJourneyId(GIVEN_JOURNEY_ID);

        PackageJourneySegmentEntity segmentEntity1 = new PackageJourneySegmentEntity();
        segmentEntity1.setRefId("0");
        segmentEntity1.setSequence("0");
        InstructionEntity instruction = new InstructionEntity();
        instruction.setId("instruction-1");
        instruction.setExternalId("ext-instruction-1");
        instruction.setSource("segment");
        instruction.setLabel("Segment X Instruction");
        instruction.setValue("Instruction Value");
        segmentEntity1.setInstructions(new ArrayList<>(List.of(instruction)));
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        shipmentJourneyEntity.addPackageJourneySegment(segmentEntity1);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPickupTimezone("testPickUpTimeZone");
        orderEntity.setDeliveryTimezone("testDeliveryTimeZone");

        ShipmentJourney previousShipmentJourney = new ShipmentJourney();
        previousShipmentJourney.setJourneyId(GIVEN_JOURNEY_ID);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organization-id1");
        when(segmentUpdateChecker.isSegmentMatch(any(), any())).thenReturn(true);
        when(shipmentJourneyRepository.save(any(ShipmentJourneyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        ShipmentJourney result = shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(
                shipmentJourneyUpdates,
                shipmentJourneyEntity,
                orderEntity
        );

        assertThat(result).isNotNull();
        assertThat(result.getJourneyId()).isEqualTo(GIVEN_JOURNEY_ID);
        assertThat(result.getPackageJourneySegments()).hasSize(1);
        assertThat(result.getPackageJourneySegments().get(0).getInstructions()).hasSize(1);
        assertThat(result.getPackageJourneySegments().get(0).getInstructions().get(0).getOrganizationId())
                .isEqualTo("organization-id1");

        verify(shipmentJourneyRepository, times(1)).save(any());
        verify(flightStatsEventService, times(1)).subscribeFlight(shipmentJourneyEntity.getPackageJourneySegments());
        verify(packageJourneySegmentService, times(1)).updateFacilityAndPartner(
                shipmentJourneyUpdates.getPackageJourneySegments(), shipmentJourneyEntity.getPackageJourneySegments()
                , orderEntity.getPickupTimezone(), orderEntity.getDeliveryTimezone());
    }

    @Test
    void givenEmptyPackageJourneySegments_whenUpdateShipmentJourney_thenSaveSuccessfully() {
        ShipmentJourney shipmentJourneyUpdates = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        Instruction newInstruction = new Instruction();
        newInstruction.setExternalId("ext-instruction-1");
        newInstruction.setSource("segment");
        newInstruction.setLabel("Segment Y Instruction");
        newInstruction.setValue("Instruction Value Z");
        packageJourneySegment.setInstructions(new ArrayList<>(List.of(newInstruction)));
        shipmentJourneyUpdates.setPackageJourneySegments(List.of(packageJourneySegment));
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("1");
        packageJourneySegmentEntity.setSequence("1");
        InstructionEntity instruction = new InstructionEntity();
        instruction.setId("instruction-1");
        instruction.setExternalId("ext-instruction-1");
        instruction.setSource("segment");
        instruction.setLabel("Segment X Instruction");
        instruction.setValue("Instruction Value");
        packageJourneySegmentEntity.setInstructions(new ArrayList<>(List.of(instruction)));
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegmentEntity);

        OrderEntity orderEntity = new OrderEntity();

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organization-id1");
        when(segmentUpdateChecker.isSegmentMatch(any(), any())).thenReturn(true);
        when(shipmentJourneyRepository.save(any())).thenReturn(shipmentJourneyEntity);

        // When
        ShipmentJourney result = shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(
                shipmentJourneyUpdates,
                shipmentJourneyEntity,
                orderEntity
        );

        assertThat(result).isNotNull();
        assertThat(result.getJourneyId()).isEqualTo(GIVEN_JOURNEY_ID);
        assertThat(result.getPackageJourneySegments()).hasSize(1);
        assertThat(result.getPackageJourneySegments().get(0).getInstructions()).hasSize(1);
        assertThat(result.getPackageJourneySegments().get(0).getInstructions().get(0).getOrganizationId())
                .isEqualTo("organization-id1");

        verify(shipmentJourneyRepository, times(1)).save(any());
        verify(flightStatsEventService, times(1)).subscribeFlight(any());
    }

    @Test
    void givenErrorData_whenUpdateShipmentJourney_thenThrowException() {
        ShipmentJourney shipmentJourneyUpdates = new ShipmentJourney();
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        shipmentJourneyUpdates.setPackageJourneySegments(List.of(packageJourneySegment));

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(GIVEN_JOURNEY_ID);
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setRefId("1");
        packageJourneySegmentEntity.setSequence("1");
        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegmentEntity);

        OrderEntity orderEntity = new OrderEntity();

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organization-id1");
        when(segmentUpdateChecker.isSegmentMatch(any(), any())).thenReturn(true);
        when(shipmentJourneyRepository.save(any(ShipmentJourneyEntity.class)))
                .thenThrow(new RuntimeException("Something went wrong."));

        try {
            // when
            shipmentJourneyWriteService.updateShipmentJourneyAndUpdateSegments(
                    shipmentJourneyUpdates,
                    shipmentJourneyEntity,
                    orderEntity
            );
        } catch (ShipmentJourneyException e) {
            assertThat(e.getMessage()).isEqualTo("Error saving Shipment Journey. Something went wrong.");
        }

        verify(shipmentJourneyRepository, times(1)).save(any(ShipmentJourneyEntity.class));
    }

    @Test
    void testClearStaleAlerts() {
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        List<AlertEntity> alerts = new ArrayList<>();
        int segmentNumber = 1;
        String message = MISSING_MANDATORY_FIELDS.getFullMessage() + String.format(SEGMENT_NO, segmentNumber);
        alerts.add(createAlertEntity(MISSING_MANDATORY_FIELDS.toString(), message, List.of("partner", "airline")));
        PackageJourneySegmentEntity segment1 = new PackageJourneySegmentEntity();
        PartnerEntity partnerEntity = new PartnerEntity();
        partnerEntity.setName("Test Partner");
        segment1.setPartner(partnerEntity);
        segment1.setAirline("Test Airline");
        shipmentJourneyEntity.addPackageJourneySegment(segment1);

        segmentNumber = 2;
        message = MISSING_MANDATORY_FIELDS.getFullMessage() + String.format(SEGMENT_NO, segmentNumber);
        alerts.add(createAlertEntity(MISSING_MANDATORY_FIELDS.toString(), message, List.of("airline_code", "flight_number")));
        PackageJourneySegmentEntity segment2 = new PackageJourneySegmentEntity();
        segment2.setAirlineCode("SG");
        segment2.setFlightNumber("12345");
        shipmentJourneyEntity.addPackageJourneySegment(segment2);

        alerts.add(createAlertEntity(FAILED_PICKUP_OR_DELIVERY.toString(), FAILED_PICKUP_OR_DELIVERY.getFullMessage(), null));
        shipmentJourneyEntity.setAlerts(alerts);

        assertThat(shipmentJourneyEntity.getAlerts()).hasSize(3);

        shipmentJourneyWriteService.cleanupAlerts(shipmentJourneyEntity);

        assertThat(shipmentJourneyEntity.getAlerts()).hasSize(1);
    }

    private AlertEntity createAlertEntity(String shortMessage, String fullMessage, List<String> fields) {
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setShortMessage(shortMessage);
        alertEntity.setMessage(fullMessage);
        alertEntity.setFields(fields);
        return alertEntity;
    }

}
