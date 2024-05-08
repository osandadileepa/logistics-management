package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.AlertLevel;
import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.exception.AlertNotFoundException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.impl.mapper.AlertMapper;
import com.quincus.shipment.impl.repository.AlertRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private static final String ERR_ALERT_NOT_FOUND = "Alert with id [%s] not found";
    private static final String ERR_UPDATE_NOT_ALLOWED = "Dismiss operation not allowed for Alert %s.";
    private final AlertRepository alertRepository;
    private final FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    private final ShipmentRepository shipmentRepository;
    private final UserDetailsProvider userDetailsProvider;
    private final AlertMapper alertMapper;
    private final ShipmentFetchService shipmentFetchService;

    public Alert createFlightNotFoundAlert(String segmentId, String sequenceNumber) {
        Alert alert = createAlertFromMessage(AlertMessage.FLIGHT_NOT_FOUND_FLIGHTSTATS, AlertType.WARNING,
                getIndex(sequenceNumber));
        alert.setPackageJourneySegmentId(segmentId);
        return alert;
    }

    public Alert createFlightNotFoundJourneyAlert(String shipmentJourneyId, String sequenceNumber) {
        Alert alert = createAlertFromMessage(AlertMessage.FLIGHT_NOT_FOUND_FLIGHTSTATS, AlertType.WARNING,
                getIndex(sequenceNumber));
        alert.setShipmentJourneyId(shipmentJourneyId);
        return alert;
    }

    public Alert createFlightCancellationAlert(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        Alert alert = createAlertFromMessage(AlertMessage.FLIGHT_CANCELLATION, AlertType.JOURNEY_REVIEW_REQUIRED,
                getIndex(packageJourneySegmentEntity.getSequence()));
        alert.setPackageJourneySegmentId(packageJourneySegmentEntity.getId());
        return alert;
    }

    public void createVendorAssignmentRejectedAlerts(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        List<AlertEntity> alertEntities = new ArrayList<>();
        AlertEntity alertEntityForSegment = createAndSaveAlertFromMessage(AlertMessage.VENDOR_ASSIGNMENT_REJECTED, AlertType.ERROR);
        alertEntityForSegment.setPackageJourneySegmentId(packageJourneySegmentEntity.getId());
        alertEntities.add(alertEntityForSegment);

        AlertEntity alertEntityForJourney = createAndSaveAlertFromMessage(AlertMessage.VENDOR_ASSIGNMENT_REJECTED, AlertType.ERROR
                , getIndex(packageJourneySegmentEntity.getSequence()));
        alertEntityForJourney.setShipmentJourneyId(packageJourneySegmentEntity.getShipmentJourneyId());
        alertEntities.add(alertEntityForJourney);

        alertRepository.saveAll(alertEntities);
    }

    public void createVendorAssignmentFailedAlerts(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        List<AlertEntity> alertEntities = new ArrayList<>();
        AlertEntity alertEntityForSegment = createAndSaveAlertFromMessage(AlertMessage.VENDOR_ASSIGNMENT_FAILED, AlertType.ERROR);
        alertEntityForSegment.setPackageJourneySegmentId(packageJourneySegmentEntity.getId());
        alertEntities.add(alertEntityForSegment);

        AlertEntity alertEntityForJourney = createAndSaveAlertFromMessage(AlertMessage.VENDOR_ASSIGNMENT_FAILED, AlertType.ERROR
                , getIndex(packageJourneySegmentEntity.getSequence()));
        alertEntityForJourney.setShipmentJourneyId(packageJourneySegmentEntity.getShipmentJourneyId());
        alertEntities.add(alertEntityForJourney);

        alertRepository.saveAll(alertEntities);
    }

    public Alert createFlightCancellationShipmentJourneyAlert(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        Alert alert = createAlertFromMessage(AlertMessage.FLIGHT_CANCELLATION, AlertType.JOURNEY_REVIEW_REQUIRED,
                getIndex(packageJourneySegmentEntity.getSequence()));
        alert.setShipmentJourneyId(packageJourneySegmentEntity.getShipmentJourneyId());
        return alert;
    }

    @Transactional
    public void saveAll(List<Alert> alerts) {
        if (CollectionUtils.isEmpty(alerts)) {
            return;
        }
        List<AlertEntity> alertEntities = alerts.stream()
                .map(alertMapper::toEntity)
                .toList();
        alertRepository.saveAllAndFlush(alertEntities);
    }

    @Transactional
    public void dismiss(String alertId, boolean dismissed) {
        AlertEntity alertEntity = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(
                        String.format(ERR_ALERT_NOT_FOUND, alertId)
                ));

        if (!facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered(alertId)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_NOT_ALLOWED, alertId),
                    ShipmentErrorCode.ALERT_DISMISS_NOT_ALLOWED);
        }

        if (alertEntity.getShipmentJourneyId() != null) {
            shipmentRepository.findByJourneyId(alertEntity.getShipmentJourneyId());
        } else if (alertEntity.getPackageJourneySegmentId() != null) {
            shipmentRepository.findBySegmentId(alertEntity.getPackageJourneySegmentId(), userDetailsProvider.getCurrentOrganizationId());
        }
        alertEntity.setDismissed(dismissed);
        alertEntity.setDismissTime(alertEntity.isDismissed() ? LocalDateTime.now(Clock.systemUTC()) : null);

        alertRepository.save(alertEntity);
    }

    @Transactional
    public void createPickupDeliveryFailedAlert(String shipmentId) {
        ShipmentEntity shipmentEntity = shipmentFetchService.findByIdOrThrowException(shipmentId);
        createPickupDeliveryFailedAlert(shipmentEntity.getShipmentJourney());
    }

    @Transactional
    public void createPickupDeliveryFailedAlert(ShipmentJourneyEntity shipmentJourneyEntity) {
        AlertEntity alertEntity = createAndSaveAlertFromMessage(AlertMessage.FAILED_PICKUP_OR_DELIVERY,
                AlertType.JOURNEY_REVIEW_REQUIRED);
        Optional.ofNullable(shipmentJourneyEntity)
                .map(ShipmentJourneyEntity::getId)
                .ifPresent(alertEntity::setShipmentJourneyId);
        alertRepository.save(alertEntity);
    }

    public List<AlertEntity> findByJourneyIdsAndSegmentIds(List<String> journeyIds, List<String> segmentIds) {
        List<Tuple> tupleList = alertRepository.findByJourneyIdsAndSegmentIds(journeyIds, segmentIds);
        if (CollectionUtils.isEmpty(tupleList)) return Collections.emptyList();
        List<AlertEntity> entities = new ArrayList<>();
        tupleList.forEach(o -> {
            AlertEntity alert = toAlertEntity(o);
            entities.add(alert);
        });
        return entities;
    }

    private Alert createAlertFromMessage(AlertMessage alertMessage, AlertType alertType, int segmentRefNum) {
        Alert alert = new Alert();
        alert.setShortMessage(alertMessage.toString());
        alert.setMessage(String.format("%s [Segment %d]", alertMessage.getFullMessage(), segmentRefNum));
        alert.setType(alertType);
        alert.setLevel(alertMessage.getLevel());
        alert.setConstraint(alertMessage.getConstraintType());
        return alert;
    }

    private AlertEntity createAndSaveAlertFromMessage(AlertMessage alertMessage, AlertType alertType) {
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setShortMessage(alertMessage.toString());
        alertEntity.setMessage(alertMessage.getFullMessage());
        alertEntity.setType(alertType);
        alertEntity.setLevel(alertMessage.getLevel());
        alertEntity.setConstraint(alertMessage.getConstraintType());
        return alertEntity;
    }

    private AlertEntity createAndSaveAlertFromMessage(AlertMessage alertMessage, AlertType alertType, int segmentRefNum) {
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setShortMessage(alertMessage.toString());
        alertEntity.setMessage(String.format("%s [Segment %d]", alertMessage.getFullMessage(), segmentRefNum));
        alertEntity.setType(alertType);
        alertEntity.setLevel(alertMessage.getLevel());
        alertEntity.setConstraint(alertMessage.getConstraintType());
        return alertEntity;
    }

    private AlertEntity toAlertEntity(Tuple tuple) {
        AlertEntity entity = new AlertEntity();
        entity.setId(tuple.get(BaseEntity_.ID, String.class));
        entity.setShortMessage(tuple.get(AlertEntity_.SHORT_MESSAGE, String.class));
        entity.setMessage(tuple.get(AlertEntity_.MESSAGE, String.class));
        entity.setType(tuple.get(AlertEntity_.TYPE, AlertType.class));
        entity.setLevel(tuple.get(AlertEntity_.LEVEL, AlertLevel.class));
        entity.setDismissed(tuple.get(AlertEntity_.DISMISSED, Boolean.class));
        entity.setDismissTime(tuple.get(AlertEntity_.DISMISS_TIME, LocalDateTime.class));
        entity.setDismissedBy(tuple.get(AlertEntity_.DISMISSED_BY, String.class));
        entity.setFields(tuple.get(AlertEntity_.FIELDS, List.class));
        entity.setShipmentJourneyId(tuple.get(AlertEntity_.SHIPMENT_JOURNEY_ID, String.class));
        entity.setPackageJourneySegmentId(tuple.get(AlertEntity_.PACKAGE_JOURNEY_SEGMENT_ID, String.class));
        entity.setConstraint(tuple.get(AlertEntity_.CONSTRAINT, ConstraintType.class));
        return entity;
    }

    private int getIndex(String sequenceNumber) {
        return Integer.parseInt(sequenceNumber) + 1;
    }
}
