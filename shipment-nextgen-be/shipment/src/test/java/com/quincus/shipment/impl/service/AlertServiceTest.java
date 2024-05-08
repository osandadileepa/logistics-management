package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.AlertLevel;
import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.exception.AlertNotFoundException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.impl.mapper.AlertMapper;
import com.quincus.shipment.impl.repository.AlertRepository;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @InjectMocks
    private AlertService alertService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private FacilityLocationPermissionChecker facilityLocationPermissionChecker;

    @Mock
    private AlertMapper alertMapper;

    @Captor
    private ArgumentCaptor<List<AlertEntity>> alertEntityArgumentCaptor;

    @Test
    @DisplayName("given unknown alertId when dismiss alert then throw error")
    void returnExpectedWhenAlertIdNotFound() {
        when(alertRepository.findById(any())).thenThrow(AlertNotFoundException.class);

        assertThatThrownBy(() -> alertService.dismiss("unknown-id", true))
                .isInstanceOf(AlertNotFoundException.class);
    }

    @Test
    @DisplayName("given alertId when dismiss alert then do not throw error")
    void dismissAlertWhenAlertIdFound() {
        when(alertRepository.findById(any())).thenReturn(Optional.of(new AlertEntity()));
        when(facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        assertThatNoException().isThrownBy(() -> alertService.dismiss("0001", true));
    }

    @Test
    @DisplayName("given alertId when dismiss alert then throw not permitted error")
    void throwErrorWhenAlertIdWithSegmentLocationNotCovered() {
        when(alertRepository.findById(any())).thenReturn(Optional.of(new AlertEntity()));
        when(facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered(anyString())).thenReturn(false);

        assertThatThrownBy(() -> alertService.dismiss("0001", true))
                .isInstanceOf(SegmentLocationNotAllowedException.class);
    }

    @Test
    @DisplayName("given shipmentJourney entity when createPickupDeliveryFailedAlert then save alert entity")
    void saveAlertEntityWhenCreatePickupDeliveryFailedAlert() {
        ShipmentJourneyEntity dummyShipmentJourney = new ShipmentJourneyEntity();

        assertThatNoException().isThrownBy(() -> alertService.createPickupDeliveryFailedAlert(dummyShipmentJourney));
        verify(alertRepository, times(1)).save(any(AlertEntity.class));
    }

    @Test
    @DisplayName("given List of alert when processAndSaveCollectedAlerts convert to entity and save")
    void testSaveListOfAlertDomain() {
        List<Alert> alertList = new ArrayList<>();

        Alert alert1 = new Alert();
        Alert alert2 = new Alert();
        alertList.add(alert1);
        alertList.add(alert2);

        AlertEntity alertEntity1 = new AlertEntity();
        AlertEntity alertEntity2 = new AlertEntity();
        when(alertMapper.toEntity(alert1)).thenReturn(alertEntity1);
        when(alertMapper.toEntity(alert2)).thenReturn(alertEntity2);
        //WHEN:
        alertService.saveAll(alertList);
        verify(alertRepository, times(1)).saveAllAndFlush(List.of(alertEntity1, alertEntity2));
    }

    @Test
    void givenShipmentJourneyIdAndSequenceNumber_whenCreateFlightNotFoundJourneyAlert_thenReturnAlertWithProperDetails() {
        String journeyId = "j-1";
        String sequence = "1";

        Alert createdAlert = alertService.createFlightNotFoundJourneyAlert(journeyId, sequence);

        assertThat(createdAlert.getLevel()).isEqualTo(AlertLevel.STANDARD);
        assertThat(createdAlert.getConstraint()).isEqualTo(ConstraintType.SOFT_CONSTRAINT);
        assertThat(createdAlert.getType()).isEqualTo(AlertType.WARNING);
        assertThat(createdAlert.getShortMessage()).contains(AlertMessage.FLIGHT_NOT_FOUND_FLIGHTSTATS.toString());
        assertThat(createdAlert.getShipmentJourneyId()).isEqualTo(journeyId);
    }

    @Test
    void givenShipmentSegmentWithJourneyIdAndSequence_whenCreateFlightCancellationShipmentJourneyAlert_thenReturnAlertWithProperDetails() {
        String journeyId = "j-1";
        String sequence = "1";

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setShipmentJourneyId(journeyId);
        packageJourneySegmentEntity.setSequence(sequence);

        Alert createdAlert = alertService.createFlightCancellationShipmentJourneyAlert(packageJourneySegmentEntity);

        assertThat(createdAlert.getLevel()).isEqualTo(AlertLevel.CRITICAL);
        assertThat(createdAlert.getConstraint()).isEqualTo(ConstraintType.HARD_CONSTRAINT);
        assertThat(createdAlert.getType()).isEqualTo(AlertType.JOURNEY_REVIEW_REQUIRED);
        assertThat(createdAlert.getShortMessage()).contains(AlertMessage.FLIGHT_CANCELLATION.toString());
        assertThat(createdAlert.getShipmentJourneyId()).isEqualTo(journeyId);
    }

    @Test
    void givenPackageJourneySegmentEntity_whenCreateVendorAssignmentFailedAlerts_thenAlertsForJourneyAndSegmentIsCreatedForFailedVendorAssignment() {
        String journeyId = "j-1";
        String segmentId = "s-1";
        String sequence = "1";

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setShipmentJourneyId(journeyId);
        packageJourneySegmentEntity.setId(segmentId);
        packageJourneySegmentEntity.setSequence(sequence);

        alertService.createVendorAssignmentFailedAlerts(packageJourneySegmentEntity);

        verify(alertRepository, times(1)).saveAll(alertEntityArgumentCaptor.capture());

        List<AlertEntity> createdAlertEntities = alertEntityArgumentCaptor.getValue();
        assertThat(createdAlertEntities).hasSize(2);
        assertThat(createdAlertEntities.get(0).getLevel()).isEqualTo(AlertLevel.CRITICAL);
        assertThat(createdAlertEntities.get(1).getLevel()).isEqualTo(AlertLevel.CRITICAL);
        assertThat(createdAlertEntities.get(0).getConstraint()).isEqualTo(ConstraintType.HARD_CONSTRAINT);
        assertThat(createdAlertEntities.get(1).getConstraint()).isEqualTo(ConstraintType.HARD_CONSTRAINT);
        assertThat(createdAlertEntities.get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(createdAlertEntities.get(1).getType()).isEqualTo(AlertType.ERROR);

        assertThat(createdAlertEntities.get(0).getShortMessage()).contains(AlertMessage.VENDOR_ASSIGNMENT_FAILED.toString());
        assertThat(createdAlertEntities.get(1).getShortMessage()).contains(AlertMessage.VENDOR_ASSIGNMENT_FAILED.toString());
        assertThat(createdAlertEntities.stream().map(AlertEntity::getPackageJourneySegmentId).filter(Objects::nonNull).anyMatch(sid -> sid.equalsIgnoreCase(segmentId))).isTrue();
        assertThat(createdAlertEntities.stream().map(AlertEntity::getShipmentJourneyId).filter(Objects::nonNull).anyMatch(shipmentJourneyId -> shipmentJourneyId.equalsIgnoreCase(journeyId))).isTrue();
    }

    @Test
    void givenPackageJourneySegmentEntity_whenCreateVendorAssignmentRejectAlerts_thenAlertsForJourneyAndSegmentIsCreatedForRejectedVendorAssignment() {
        String journeyId = "j-1";
        String segmentId = "s-1";
        String sequence = "1";

        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setShipmentJourneyId(journeyId);
        packageJourneySegmentEntity.setId(segmentId);
        packageJourneySegmentEntity.setSequence(sequence);

        alertService.createVendorAssignmentRejectedAlerts(packageJourneySegmentEntity);

        verify(alertRepository, times(1)).saveAll(alertEntityArgumentCaptor.capture());

        List<AlertEntity> createdAlertEntities = alertEntityArgumentCaptor.getValue();
        assertThat(createdAlertEntities).hasSize(2);
        assertThat(createdAlertEntities.get(0).getLevel()).isEqualTo(AlertLevel.CRITICAL);
        assertThat(createdAlertEntities.get(1).getLevel()).isEqualTo(AlertLevel.CRITICAL);
        assertThat(createdAlertEntities.get(0).getConstraint()).isEqualTo(ConstraintType.HARD_CONSTRAINT);
        assertThat(createdAlertEntities.get(1).getConstraint()).isEqualTo(ConstraintType.HARD_CONSTRAINT);
        assertThat(createdAlertEntities.get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(createdAlertEntities.get(1).getType()).isEqualTo(AlertType.ERROR);

        assertThat(createdAlertEntities.get(0).getShortMessage()).contains(AlertMessage.VENDOR_ASSIGNMENT_REJECTED.toString());
        assertThat(createdAlertEntities.get(1).getShortMessage()).contains(AlertMessage.VENDOR_ASSIGNMENT_REJECTED.toString());
        assertThat(createdAlertEntities.stream().map(AlertEntity::getPackageJourneySegmentId).filter(Objects::nonNull).anyMatch(sid -> sid.equalsIgnoreCase(segmentId))).isTrue();
        assertThat(createdAlertEntities.stream().map(AlertEntity::getShipmentJourneyId).filter(Objects::nonNull).anyMatch(shipmentJourneyId -> shipmentJourneyId.equalsIgnoreCase(journeyId))).isTrue();
    }

}
