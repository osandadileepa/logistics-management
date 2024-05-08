package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentEnrichmentServiceTest {

    @InjectMocks
    private ShipmentEnrichmentService shipmentEnrichmentService;
    @Mock
    private AlertService alertService;
    @Mock
    private InstructionFetchService instructionFetchService;

    @Test
    void givenInstructionForSegment_whenEnrichShipmentPackageJourneySegmentsWithInstructions_properlyEnrichInstructionsToSegment() {
        // Given:
        InstructionEntity instructionEntity1 = mock(InstructionEntity.class);
        when(instructionEntity1.getPackageJourneySegmentId()).thenReturn("1");
        InstructionEntity instructionEntity2 = mock(InstructionEntity.class);
        when(instructionEntity2.getPackageJourneySegmentId()).thenReturn("1");
        InstructionEntity instructionEntity3 = mock(InstructionEntity.class);
        when(instructionEntity3.getPackageJourneySegmentId()).thenReturn("2");
        InstructionEntity instructionEntity4 = mock(InstructionEntity.class);
        when(instructionEntity4.getPackageJourneySegmentId()).thenReturn("1");
        List<InstructionEntity> instructions = List.of(instructionEntity1, instructionEntity2, instructionEntity3, instructionEntity4);

        List<PackageJourneySegmentEntity> packageJourneySegments = createPackageJourneySegments();
        List<String> segmentIds = packageJourneySegments.stream().map(PackageJourneySegmentEntity::getId).toList();

        when(instructionFetchService.findBySegmentIds(segmentIds)).thenReturn(instructions);
        // When:
        shipmentEnrichmentService.enrichShipmentPackageJourneySegmentsWithInstructions(packageJourneySegments);
        // Then:
        packageJourneySegments.forEach(segment -> assertThat(segment.getInstructions()).isNotEmpty());
        assertThat(packageJourneySegments.get(0).getInstructions()).hasSize(3);
        assertThat(packageJourneySegments.get(1).getInstructions()).hasSize(1);
        verify(instructionFetchService).findBySegmentIds(segmentIds);
    }

    @Test
    void givenShipmentJourneyWithSegments_whenEnrichShipmentJourneyAndSegmentWithAlert_properlyEnrichAlertsToJourneyAndSegments() {
        // Given:
        AlertEntity alertEntity1 = mock(AlertEntity.class);
        when(alertEntity1.getPackageJourneySegmentId()).thenReturn("1");
        AlertEntity alertEntity2 = mock(AlertEntity.class);
        when(alertEntity2.getPackageJourneySegmentId()).thenReturn("1");
        AlertEntity alertEntity3 = mock(AlertEntity.class);
        when(alertEntity3.getPackageJourneySegmentId()).thenReturn("1");
        AlertEntity alertEntity4 = mock(AlertEntity.class);
        when(alertEntity4.getPackageJourneySegmentId()).thenReturn("1");
        AlertEntity alertEntity5 = mock(AlertEntity.class);
        when(alertEntity5.getPackageJourneySegmentId()).thenReturn("2");
        AlertEntity alertEntity6 = mock(AlertEntity.class);
        when(alertEntity6.getPackageJourneySegmentId()).thenReturn("2");

        AlertEntity alertEntity7 = mock(AlertEntity.class);
        when(alertEntity7.getShipmentJourneyId()).thenReturn("j1");
        AlertEntity alertEntity8 = mock(AlertEntity.class);
        when(alertEntity8.getShipmentJourneyId()).thenReturn("j1");
        List<AlertEntity> alertEntities = List.of(alertEntity1, alertEntity2, alertEntity3, alertEntity4
                , alertEntity5, alertEntity6, alertEntity7, alertEntity8);

        ShipmentJourneyEntity shipmentJourneyEntity = createShipmentJourney();
        List<PackageJourneySegmentEntity> packageJourneySegments = createPackageJourneySegments();
        shipmentJourneyEntity.addAllPackageJourneySegments(packageJourneySegments);
        List<String> segmentIds = packageJourneySegments.stream().map(PackageJourneySegmentEntity::getId).toList();

        when(alertService.findByJourneyIdsAndSegmentIds(List.of(shipmentJourneyEntity.getId()), segmentIds)).thenReturn(alertEntities);
        // When:
        shipmentEnrichmentService.enrichShipmentJourneyAndSegmentWithAlert(shipmentJourneyEntity);
        // Then:
        packageJourneySegments.forEach(segment -> assertThat(segment.getAlerts()).isNotEmpty());
        assertThat(packageJourneySegments.get(0).getAlerts()).hasSize(4);
        assertThat(packageJourneySegments.get(1).getAlerts()).hasSize(2);
        assertThat(shipmentJourneyEntity.getAlerts()).hasSize(2);
        verify(alertService).findByJourneyIdsAndSegmentIds(List.of(shipmentJourneyEntity.getId()), segmentIds);
    }


    private List<PackageJourneySegmentEntity> createPackageJourneySegments() {
        PackageJourneySegmentEntity segment1 = new PackageJourneySegmentEntity();
        segment1.setId("1");

        PackageJourneySegmentEntity segment2 = new PackageJourneySegmentEntity();
        segment2.setId("2");

        return List.of(segment1, segment2);
    }

    private ShipmentJourneyEntity createShipmentJourney() {
        ShipmentJourneyEntity journey = new ShipmentJourneyEntity();
        journey.setId("j1");
        return journey;
    }

}
