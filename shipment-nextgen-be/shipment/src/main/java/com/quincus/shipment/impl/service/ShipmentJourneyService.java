package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentJourneyContext;
import com.quincus.shipment.api.exception.PartnerNotAllowedException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.api.exception.ShipmentJourneyMismatchException;
import com.quincus.shipment.impl.helper.SegmentReferenceHolder;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.repository.ShipmentJourneyRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.PackageJourneySegmentValidator;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import liquibase.repackaged.org.apache.commons.lang3.tuple.Pair;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ShipmentJourneyService {
    static final String ERR_UPDATE_LOCATION_NOT_COVERED = "Current user does not meet the required location permissions to access or modify Shipment Journey %s.";
    static final String ERR_UPDATE_PARTNER_NOT_ALLOWED = "Current user does not meet the required user group to access or modify Shipment Journey %s.";
    private static final String ERR_UPDATE_SHIPMENT_JOURNEY_MISMATCH = "Error updating Shipment Journey. The Shipment Journey " +
            "with id %s does not belong to Shipment with id %s and Order Id %s";
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final UserDetailsProvider userDetailsProvider;
    private final ObjectMapper objectMapper;
    private final ShipmentJourneyAsyncService shipmentJourneyAsyncService;
    private final ShipmentFetchService shipmentFetchService;
    private final ShipmentJourneyWriteService shipmentJourneyWriteService;
    private final PackageJourneySegmentValidator packageJourneySegmentValidator;
    private final PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    private final ShipmentJourneyRepository journeyRepository;
    private final SegmentReferenceProvider segmentReferenceProvider;

    @Transactional
    public ShipmentJourneyEntity create(ShipmentJourney shipmentJourney, Order order) {
        packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourney);
        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        SegmentReferenceHolder segmentReferenceHolder = segmentReferenceProvider.generateReference(shipmentJourney.getPackageJourneySegments());
        ShipmentJourneyEntity baseJourney = ShipmentJourneyMapper.mapDomainToEntity(shipmentJourney, false);
        List<PackageJourneySegmentEntity> segmentEntities = shipmentJourney.getPackageJourneySegments().stream()
                .map(segment -> packageJourneySegmentService.initializeSegmentFacilitiesAndPartner(segmentReferenceHolder,
                        segment, order.getPickupTimezone(), order.getDeliveryTimezone()))
                .map(segmentEntity -> packageJourneySegmentService.enrichSegmentWithJourneyInformation(segmentEntity, baseJourney))
                .map(segmentEntity -> packageJourneySegmentService.enrichSegmentWithOrderOpsTypeInformation(segmentEntity, order))
                .collect(Collectors.toCollection(ArrayList::new));
        baseJourney.addAllPackageJourneySegments(segmentEntities);
        return baseJourney;
    }

    @Transactional
    public ShipmentJourneyEntity update(ShipmentJourney shipmentJourney, ShipmentJourneyEntity shipmentJourneyEntity,
                                        Order order, boolean segmentsUpdated) {
        if (!segmentsUpdated) return shipmentJourneyEntity;
        packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourney);
        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, true);
        shipmentJourneyWriteService.updateShipmentJourneyEntityFromDomain(shipmentJourney, shipmentJourneyEntity, order);
        return shipmentJourneyEntity;
    }

    @Retryable(value = {ShipmentJourneyException.class}, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
    @LogExecutionTime
    @Transactional
    public List<String> validateAndUpdateShipmentJourney(ShipmentJourney shipmentJourneyUpdates) {

        final List<ShipmentEntity> shipmentsWithSameJourney = extractAndValidateShipmentWithSameJourney(shipmentJourneyUpdates);
        final ShipmentEntity shipmentEntity = shipmentsWithSameJourney.get(0);
        final ShipmentJourneyEntity shipmentJourneyEntity = shipmentEntity.getShipmentJourney();
        ShipmentJourney previousShipmentJourney = ShipmentJourneyMapper.mapEntityToDomain(shipmentJourneyEntity);
        previousShipmentJourney.markAllSegmentsAsDeleted();

        final ShipmentJourney updatedShipmentJourney = shipmentJourneyWriteService
                .updateShipmentJourneyAndUpdateSegments(shipmentJourneyUpdates, shipmentJourneyEntity, shipmentEntity.getOrder());
        packageJourneySegmentService.enrichSegmentsWithOrderInstructions(shipmentJourneyUpdates.getOrderId(),
                updatedShipmentJourney.getPackageJourneySegments());

        List<String> shipmentIdsWithSameJourney = shipmentsWithSameJourney.stream().map(ShipmentEntity::getId).toList();
        shipmentJourneyAsyncService.sendShipmentJourneyUpdates(shipmentIdsWithSameJourney, previousShipmentJourney, updatedShipmentJourney);
        return extractShipmentTrackingIdsWithSameJourney(shipmentsWithSameJourney);
    }

    @Retryable(value = {ShipmentJourneyException.class}, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
    @LogExecutionTime
    @Transactional
    public ShipmentJourneyContext updateShipmentJourney(ShipmentJourney toUpdateShipmentJourney) {

        final List<ShipmentEntity> shipmentsWithSameJourney = extractAndValidateShipmentWithSameJourney(toUpdateShipmentJourney);
        final ShipmentEntity shipmentEntity = shipmentsWithSameJourney.get(0);
        final ShipmentJourneyEntity shipmentJourneyEntity = shipmentEntity.getShipmentJourney();

        ShipmentJourney previousShipmentJourney = ShipmentJourneyMapper.mapEntityToDomain(shipmentJourneyEntity);
        ShipmentJourney updatedShipmentJourney = shipmentJourneyWriteService
                .updateShipmentJourneyAndUpdateSegments(toUpdateShipmentJourney, shipmentJourneyEntity, shipmentEntity.getOrder());
        packageJourneySegmentService.enrichSegmentsWithOrderInstructions(toUpdateShipmentJourney.getOrderId(),
                updatedShipmentJourney.getPackageJourneySegments());

        Pair<List<String>, List<String>> extractedIdAndTrackingIds = extractShipmentTrackingIdAndIds(shipmentsWithSameJourney);

        return new ShipmentJourneyContext()
                .previousShipmentJourney(previousShipmentJourney)
                .updatedShipmentJourney(updatedShipmentJourney)
                .shipmentTrackingIds(extractedIdAndTrackingIds.getRight())
                .shipmentIds(extractedIdAndTrackingIds.getLeft());
    }

    @Transactional
    public void sendShipmentJourneyUpdates(ShipmentJourneyContext shipmentJourneyContext) {
        shipmentJourneyAsyncService.sendShipmentJourneyUpdates(shipmentJourneyContext);
    }

    @Transactional
    public ShipmentJourneyEntity createShipmentJourneyEntity(Shipment shipment, List<ShipmentEntity> existingShipments, boolean segmentsUpdated) {
        ShipmentJourney shipmentJourney = shipment.getShipmentJourney();
        Optional<ShipmentEntity> shipmentEntityOptional = existingShipments.stream()
                .filter(shp -> shp.getShipmentTrackingId().equalsIgnoreCase(shipment.getShipmentTrackingId()))
                .findFirst();
        Order order = shipment.getOrder();
        ShipmentJourneyEntity shipmentJourneyEntity = shipmentEntityOptional.map(shipmentEntity ->
                        update(shipmentJourney, shipmentEntity.getShipmentJourney(), order, segmentsUpdated))
                .orElseGet(() -> create(shipmentJourney, order));

        if (shipmentEntityOptional.isEmpty() || !segmentsUpdated) {
            // Saving of newly created journey relies on the cascade save later on
            return shipmentJourneyEntity;
        }

        packageJourneySegmentService.updateFacilityAndPartner(shipmentJourney.getPackageJourneySegments(),
                shipmentJourneyEntity.getPackageJourneySegments(), order.getPickupTimezone(), order.getDeliveryTimezone());

        return journeyRepository.save(shipmentJourneyEntity);
    }

    @Transactional
    public ShipmentJourneyEntity save(ShipmentJourneyEntity journeyEntity) {
        return journeyRepository.save(journeyEntity);
    }

    private List<String> extractShipmentTrackingIdsWithSameJourney(List<ShipmentEntity> shipmentsWithSameJourney) {
        return shipmentsWithSameJourney.stream()
                .map(ShipmentEntity::getShipmentTrackingId)
                .toList();
    }

    private List<ShipmentEntity> extractAndValidateShipmentWithSameJourney(ShipmentJourney shipmentJourneyUpdates) {
        validateJourneyLocationPermissions(shipmentJourneyUpdates);
        final List<ShipmentEntity> shipmentEntitiesWithSameJourney = shipmentFetchService
                .findShipmentsForShipmentJourneyUpdate(objectMapper, shipmentJourneyUpdates.getOrderId(), shipmentJourneyUpdates.getJourneyId());
        if (CollectionUtils.isEmpty(shipmentEntitiesWithSameJourney)) {
            throw new ShipmentJourneyMismatchException(String.format(ERR_UPDATE_SHIPMENT_JOURNEY_MISMATCH, shipmentJourneyUpdates.getJourneyId(), shipmentJourneyUpdates.getShipmentId(), shipmentJourneyUpdates.getOrderId()));
        }
        validateUserPartnerPermissions(shipmentEntitiesWithSameJourney, shipmentJourneyUpdates);
        return shipmentEntitiesWithSameJourney;
    }

    private void validateJourneyLocationPermissions(ShipmentJourney previousShipmentJourney) {
        if (!userDetailsProvider.isFromAllowedSource()
                && !isAnyLocationAllowedOnPackageJourneySegments(previousShipmentJourney)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_LOCATION_NOT_COVERED, previousShipmentJourney.getJourneyId()), ShipmentErrorCode.SEGMENT_LOCATION_UPSERT_NOT_ALLOWED);
        }
    }

    private void validateUserPartnerPermissions(List<ShipmentEntity> shipmentList, ShipmentJourney refShipmentJourney) {
        String userPartnerId = userDetailsProvider.getCurrentPartnerId();
        List<String> userPartnersId = userDetailsProvider.getCurrentUserPartners().stream()
                .map(QuincusUserPartner::getPartnerId).collect(Collectors.toCollection(ArrayList::new));
        List<String> uniqueShipmentPartners = shipmentList.stream().map(ShipmentEntity::getPartnerId)
                .distinct().toList();
        if (userPartnerId != null) {
            userPartnersId.add(userPartnerId);
            if (Collections.disjoint(userPartnersId, uniqueShipmentPartners)) {
                throw new PartnerNotAllowedException(String.format(ERR_UPDATE_PARTNER_NOT_ALLOWED, refShipmentJourney.getJourneyId()),
                        ShipmentErrorCode.RESOURCE_ACCESS_FORBIDDEN);
            }
        } else {
            if (!uniqueShipmentPartners.contains(null) && Collections.disjoint(userPartnersId, uniqueShipmentPartners)) {
                throw new PartnerNotAllowedException(String.format(ERR_UPDATE_PARTNER_NOT_ALLOWED, refShipmentJourney.getJourneyId()),
                        ShipmentErrorCode.RESOURCE_ACCESS_FORBIDDEN);
            }
        }
    }

    public boolean isAnyLocationAllowedOnPackageJourneySegments(@NonNull final ShipmentJourney journey) {
        if (CollectionUtils.isEmpty(journey.getPackageJourneySegments())) {
            return false;
        }

        return journey.getPackageJourneySegments().stream()
                .anyMatch(packageJourneySegmentService::isSegmentAllFacilitiesAllowed);
    }

    private Pair<List<String>, List<String>> extractShipmentTrackingIdAndIds(List<ShipmentEntity> shipmentsWithSameJourney) {
        List<String> shipmentIds = new ArrayList<>();
        List<String> shipmentTrackingIds = new ArrayList<>();
        for (ShipmentEntity shipment : shipmentsWithSameJourney) {
            shipmentIds.add(shipment.getId());
            shipmentTrackingIds.add(shipment.getShipmentTrackingId());
        }
        return Pair.of(shipmentIds, shipmentTrackingIds);
    }
}
