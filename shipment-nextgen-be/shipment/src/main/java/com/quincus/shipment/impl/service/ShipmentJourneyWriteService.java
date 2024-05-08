package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.impl.helper.InstructionUtil;
import com.quincus.shipment.impl.helper.SegmentReferenceHolder;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.mapper.OrderMapper;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.repository.ShipmentJourneyRepository;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.quincus.shipment.impl.mapper.ShipmentJourneyMapper.mapEntityToDomain;
import static java.time.Instant.now;
import static java.util.Objects.isNull;

@Slf4j
@Service
@AllArgsConstructor
public class ShipmentJourneyWriteService {
    private static final String ERROR_SAVING_SHIPMENT_JOURNEY = "Error saving Shipment Journey. %s";
    private static final String UPDATING_SHIPMENT_JOURNEY = "Updating Shipment Journey with ID '%s'";
    private final UserDetailsProvider userDetailsProvider;
    private final ShipmentJourneyRepository shipmentJourneyRepository;
    private final FlightStatsEventService flightStatsEventService;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final SegmentUpdateChecker segmentUpdateChecker;
    private final SegmentReferenceProvider segmentReferenceProvider;

    @Transactional
    public ShipmentJourney updateShipmentJourneyAndUpdateSegments(
            @NonNull final ShipmentJourney shipmentJourneyUpdates,
            @NonNull final ShipmentJourneyEntity shipmentJourneyEntity,
            @NonNull final OrderEntity orderEntity) {

        Order order = OrderMapper.mapEntityToDomain(orderEntity);
        updateShipmentJourneyEntityFromDomain(shipmentJourneyUpdates, shipmentJourneyEntity, order);
        List<PackageJourneySegment> packageJourneySegments = shipmentJourneyUpdates.getPackageJourneySegments();
        List<PackageJourneySegmentEntity> packageJourneySegmentEntityList = shipmentJourneyEntity.getPackageJourneySegments();
        if (CollectionUtils.isEmpty(packageJourneySegments) || CollectionUtils.isEmpty(packageJourneySegmentEntityList)) {
            return saveShipmentJourney(shipmentJourneyEntity);
        }
        packageJourneySegmentEntityList
                .forEach(segment -> InstructionUtil.enrichNewInstructions(segment.getInstructions(), userDetailsProvider.getCurrentOrganizationId()));
        packageJourneySegmentService.updateFacilityAndPartner(packageJourneySegments,
                packageJourneySegmentEntityList, order.getPickupTimezone(), order.getDeliveryTimezone());
        flightStatsEventService.subscribeFlight(shipmentJourneyEntity.getPackageJourneySegments());
        return saveShipmentJourney(shipmentJourneyEntity);
    }

    public void updateShipmentJourneyEntityFromDomain(ShipmentJourney shipmentJourneyDomain,
                                                      ShipmentJourneyEntity shipmentJourneyEntity,
                                                      Order order) {
        if (isNull(shipmentJourneyDomain)) {
            return;
        }
        if (isNull(shipmentJourneyEntity)) {
            shipmentJourneyEntity = new ShipmentJourneyEntity();
        }
        log.debug(String.format(UPDATING_SHIPMENT_JOURNEY, shipmentJourneyEntity.getId()));
        shipmentJourneyEntity.setStatus(shipmentJourneyDomain.getStatus());
        shipmentJourneyEntity.setModifyTime(now(Clock.systemUTC()));
        cleanupAlerts(shipmentJourneyEntity);
        ShipmentJourneyMapper.setJourneyEntityAlerts(shipmentJourneyEntity, shipmentJourneyDomain);

        List<PackageJourneySegment> newOrUpdatedSegments = shipmentJourneyDomain.getPackageJourneySegments();
        List<PackageJourneySegmentEntity> existingSegments = shipmentJourneyEntity.getPackageJourneySegments();
        updateOrDeleteOrAddSegments(existingSegments, newOrUpdatedSegments, shipmentJourneyEntity, order);
    }

    protected void cleanupAlerts(ShipmentJourneyEntity shipmentJourneyEntity) {
        List<AlertEntity> journeyEntityAlerts = shipmentJourneyEntity.getAlerts();
        if (CollectionUtils.isNotEmpty(journeyEntityAlerts)) {
            shipmentJourneyEntity.setAlerts(filterAlerts(journeyEntityAlerts));
        }

        List<PackageJourneySegmentEntity> packageJourneySegmentEntities = shipmentJourneyEntity.getPackageJourneySegments();
        if (CollectionUtils.isNotEmpty(packageJourneySegmentEntities)) {
            packageJourneySegmentEntities.forEach(segmentEntity -> {
                List<AlertEntity> segmentAlerts = segmentEntity.getAlerts();
                if (CollectionUtils.isNotEmpty(segmentAlerts)) {
                    segmentEntity.setAlerts(filterAlerts(segmentAlerts));
                }
            });
        }
    }

    private List<AlertEntity> filterAlerts(List<AlertEntity> alertEntities) {
        if (org.springframework.util.CollectionUtils.isEmpty(alertEntities)) return Collections.emptyList();
        List<AlertEntity> filteredAlertEntities = new ArrayList<>();
        alertEntities.forEach(alert -> {
            if (!AlertMessage.getJourneyPageTriggeredAlerts().contains(alert.getShortMessage())) {
                filteredAlertEntities.add(alert);
            }
        });
        return filteredAlertEntities;
    }

    private void updateOrDeleteOrAddSegments(List<PackageJourneySegmentEntity> existingSegments,
                                             List<PackageJourneySegment> segments, ShipmentJourneyEntity refJourneyEntity,
                                             Order order) {
        List<PackageJourneySegment> newOrUpdatedSegments = new ArrayList<>(segments);
        existingSegments.stream().filter(Predicate.not(PackageJourneySegmentEntity::isDeleted)).forEach(segmentEntity -> {
            segmentEntity.setDeleted(true);
            for (Iterator<PackageJourneySegment> iterator = newOrUpdatedSegments.iterator(); iterator.hasNext(); ) {
                PackageJourneySegment newOrUpdatedSegment = iterator.next();
                if (segmentUpdateChecker.isSegmentMatch(newOrUpdatedSegment, segmentEntity)) {
                    PackageJourneySegmentMapper.mapDomainToExistingEntity(segmentEntity, newOrUpdatedSegment);
                    InstructionUtil.updateInstructionList(segmentEntity.getInstructions(),
                            newOrUpdatedSegment.getInstructions());
                    segmentEntity.setDeleted(false);
                    iterator.remove();
                }
            }
        });
        Instant deleteTime = Instant.now(Clock.systemUTC());
        List<PackageJourneySegmentEntity> refExistingSegments = existingSegments.stream()
                .filter(PackageJourneySegmentEntity::isDeleted).toList();
        refExistingSegments.forEach(segmentEntity -> segmentEntity.setModifyTime(deleteTime));

        SegmentReferenceHolder segmentReferenceHolder = segmentReferenceProvider.generateReference(newOrUpdatedSegments);
        List<PackageJourneySegmentEntity> newSegmentEntities = newOrUpdatedSegments.stream()
                .map(segment -> segmentUpdateChecker.findSegmentForUpdate(segment, refExistingSegments)
                        .orElse(createNewSegmentEntityFromJourney(segmentReferenceHolder, segment, refJourneyEntity,
                                order)))
                .collect(Collectors.toCollection(ArrayList::new));
        existingSegments.addAll(newSegmentEntities);
    }

    private PackageJourneySegmentEntity createNewSegmentEntityFromJourney(SegmentReferenceHolder segmentReferenceHolder,
                                                                          PackageJourneySegment segment,
                                                                          ShipmentJourneyEntity refJourneyEntity,
                                                                          Order order) {
        PackageJourneySegmentEntity segmentEntity = packageJourneySegmentService.initializeSegmentFacilitiesAndPartner(segmentReferenceHolder, segment,
                order.getPickupTimezone(), order.getDeliveryTimezone());
        segmentEntity = packageJourneySegmentService.enrichSegmentWithJourneyInformation(segmentEntity, refJourneyEntity);
        return packageJourneySegmentService.enrichSegmentWithOrderOpsTypeInformation(segmentEntity, order);
    }

    private ShipmentJourney saveShipmentJourney(ShipmentJourneyEntity entity) {
        try {
            return mapEntityToDomain(shipmentJourneyRepository.save(entity));
        } catch (Exception e) {
            throw new ShipmentJourneyException(String.format(ERROR_SAVING_SHIPMENT_JOURNEY, e.getMessage()));
        }
    }
}
