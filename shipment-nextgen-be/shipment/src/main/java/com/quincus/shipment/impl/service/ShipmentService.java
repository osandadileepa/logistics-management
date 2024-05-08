package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.ext.annotation.Utility;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.ShipmentResult;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateResponse;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentIdMismatchException;
import com.quincus.shipment.api.exception.ShipmentInvalidStatusException;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.api.filter.ShipmentFilterResult;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.helper.CreateShipmentHelper;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.PackageJourneySegmentValidator;
import com.quincus.shipment.impl.validator.ShipmentValidator;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.quincus.shipment.impl.mapper.ShipmentMapper.mapEntityToDomain;
import static com.quincus.web.logging.LoggingUtil.getTransactionId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
@AllArgsConstructor
public class ShipmentService {
    static final String ERR_UPDATE_NOT_ALLOWED = "Update operation not allowed for Shipment %s.";
    static final String ERR_READ_NOT_ALLOWED = "Read operation not allowed for Shipment %s.";
    static final String ERR_UPDATE_MILESTONE_OTHER_INFO_NOT_ALLOWED = "Update operation for milestone additional info not allowed for Shipment %s.";
    static final String ERR_CANCEL_NOT_ALLOWED = "Cancel operation not allowed for Shipment %s.";
    private static final String ERR_UPDATE_SHIPMENT_MISMATCH = "Error updating Shipment Entity with " +
            "[ ShipmentTrackingId: %s, OrderId: %s, OrganizationId: %s ] due to mismatch Shipment Domain with " +
            "[ ShipmentTrackingId: %s, OrderId: %s, OrganizationId: %s ]";
    private static final String ERR_SHIPMENT_ALREADY_CANCELLED = "Shipment Id %s already cancelled.";
    private static final String SHIPMENT_CREATED_FROM_ORDER = "SHIPMENT CREATED {} From Order {} with TrackingId {} ";
    private static final String SHIPMENT_UPDATED = "SHIPMENT UPDATED with ShipmentId {}, OrderId {}, TrackingId {} ";
    private static final String SHIPMENT_CANCELLED = "SHIPMENT CANCELLED with ShipmentId {}, OrderId {}, TrackingId {} ";
    private static final String SHIPMENT_DELETED = "SHIPMENT DELETED with ShipmentId {} ";
    private static final String CANCELLED = "cancelled";
    private static final String ERR_MILESTONE_NULL = "Milestone or Milestone code is null skipping sending of milestone update.";
    private static final String INFO_CHECKING_RETRY_TO_DISPATCH_ALLOWED = "Checking if a retry is allowed for shipment id: {}";
    private static final String WARN_RETRY_TO_DISPATCH_NOT_ALLOWED = "Retry is not allowed, creating an alert message.";
    private static final String UPDATE_ON_CANCELLED_SHIPMENT_NOT_ALLOWED = "Update on cancelled shipment not allowed!";
    private static final String SHIPMENT_SERVICE_CANCEL_BY_ID = "ShipmentService#cancelById";
    private final ShipmentRepository shipmentRepository;
    private final CreateShipmentHelper createShipmentHelper;
    private final UpdateShipmentHelper updateShipmentHelper;
    private final MilestoneService milestoneService;
    private final MilestoneHubLocationHandler milestoneHubLocationHandler;
    private final MessageApi messageApi;
    private final ObjectMapper objectMapper;
    private final QLoggerAPI qLoggerAPI;
    private final AlertService alertService;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    private final FlightStatsEventService flightStatsEventService;
    private final ShipmentProjectionListingPage projectionListingPage;
    private final UserDetailsProvider userDetailsProvider;
    private final ShipmentCriteriaMapper shipmentCriteriaMapper;
    private final LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    private final UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    private final PackageDimensionService packageDimensionService;
    private final List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    private final PackageJourneySegmentRepository packageJourneySegmentRepository;
    private final PackageJourneySegmentValidator packageJourneySegmentValidator;
    private final ShipmentJourneyService shipmentJourneyService;
    private final ShipmentFetchService shipmentFetchService;
    private final NotificationApi notificationApi;
    private final ShipmentEnrichmentService shipmentEnrichmentService;
    private final ShipmentPostProcessService shipmentPostProcessService;
    private final MilestonePostProcessService milestonePostProcessService;
    private final OrderShipmentMetadataService orderShipmentMetadataService;
    private final ShipmentValidator shipmentValidator;
    private final PackageLogService packageLogService;
    private final MilestoneTimezoneHelper milestoneTimezoneHelper;
    private final SegmentUpdateChecker segmentUpdateChecker;

    @Transactional
    public List<Shipment> createOrUpdate(final List<Shipment> shipments, boolean segmentsUpdated) {
        Shipment firstShipment = shipments.get(0);
        shipmentValidator.validateShipment(firstShipment);
        List<ShipmentEntity> existingShipments = shipmentRepository.findAllByOrderId(firstShipment.getOrder().getId(), userDetailsProvider.getCurrentOrganizationId());
        Shipment refShipment = getRefShipment(shipments, existingShipments);
        ShipmentJourneyEntity commonShipmentJourney = shipmentJourneyService.createShipmentJourneyEntity(refShipment, existingShipments, segmentsUpdated);

        List<ShipmentEntity> missingShipments = new ArrayList<>();
        List<Shipment> newShipments = new ArrayList<>(shipments);
        List<Shipment> updatedShipments = new ArrayList<>();

        identifyAndUpdateShipments(existingShipments, commonShipmentJourney, missingShipments, newShipments, updatedShipments);

        if (CollectionUtils.isNotEmpty(newShipments)) {
            OrderShipmentMetadata orderShipmentMetadata = orderShipmentMetadataService.createOrderShipmentMetaData(refShipment, commonShipmentJourney);
            for (Shipment shipment : newShipments) {
                createByOrderShipmentMetadata(shipment, orderShipmentMetadata);
            }
        }

        List<Shipment> createdOrUpdatedShipments = new ArrayList<>(updatedShipments);
        createdOrUpdatedShipments.addAll(newShipments);
        ShipmentJourney refJourney = createdOrUpdatedShipments.get(0).getShipmentJourney();
        setJourneyAndSegmentIds(refJourney, commonShipmentJourney);

        String orderId = refShipment.getOrder().getId();
        List<PackageJourneySegment> refSegments = refJourney.getPackageJourneySegments();
        packageJourneySegmentService.enrichSegmentsWithOrderInstructions(orderId, refSegments);

        adjustSegmentFromMissingShipments(missingShipments, commonShipmentJourney);
        flightStatsEventService.subscribeFlight(commonShipmentJourney.getPackageJourneySegments());
        return createdOrUpdatedShipments;
    }

    @Transactional
    public Shipment create(Shipment shipment, ShipmentJourneyEntity shipmentJourneyEntity) {
        validateUniqueShipmentCreation(shipment);
        shipment.setOrganization(userDetailsProvider.getCurrentOrganization());
        shipment.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        Shipment createdShipment = createShipment(shipment, shipmentJourneyEntity);
        packageLogService.createPackageLogForShipmentPackage(createdShipment.getId(), createdShipment.getShipmentPackage());
        shipmentPostProcessService.publishQloggerCreateEvents(createdShipment);
        return createdShipment;
    }

    @Transactional
    public Shipment createShipmentThenSendJourneyToOtherProducts(Shipment shipment, ShipmentJourneyEntity shipmentJourneyEntity) {
        Shipment createdShipment = create(shipment, shipmentJourneyEntity);
        messageApi.sendShipmentToQShip(createdShipment);
        flightStatsEventService.subscribeFlight(shipmentJourneyEntity.getPackageJourneySegments());
        return createdShipment;
    }

    @Transactional
    public Shipment update(Shipment shipment, boolean segmentsUpdated) {
        shipment.setOrganization(userDetailsProvider.getCurrentOrganization());
        String shipmentId = shipment.getId();
        ShipmentEntity entity = shipmentFetchService.findByIdOrThrowException(shipmentId);
        if (!isShipmentAnySegmentLocationAllowed(entity)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_NOT_ALLOWED, shipmentId),
                    ShipmentErrorCode.SEGMENT_LOCATION_UPSERT_NOT_ALLOWED);
        }

        ShipmentJourneyEntity shipmentJourneyEntity = createShipmentJourneyEntity(shipment, segmentsUpdated);
        Shipment updatedShipment = update(shipment, entity, shipmentJourneyEntity);
        shipmentPostProcessService.sendUpdateToQship(updatedShipment);
        flightStatsEventService.subscribeFlight(entity.getShipmentJourney().getPackageJourneySegments());
        return updatedShipment;
    }

    private Shipment update(Shipment shipment, ShipmentEntity entity, ShipmentJourneyEntity shipmentJourneyEntity) {
        if (StringUtils.equalsIgnoreCase(shipment.getOrder().getStatus(), CANCELLED)) {
            handleCancelledOrder(shipment, entity);
            return shipment;
        }

        ShipmentEntity updatedShipmentEntity = updateShipment(shipment, entity);
        updatedShipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        Shipment updatedShipment = mapEntityToDomain(updatedShipmentEntity, objectMapper);
        updatedShipment.setSegmentsUpdatedFromSource(shipment.isSegmentsUpdatedFromSource());
        sendUpdatesToQlogger(shipment, entity, updatedShipment);
        handlePackageDimensionUpdate(entity, updatedShipment);
        updatedShipment.setUpdated(true);
        return updatedShipment;
    }

    private Shipment createShipment(Shipment shipment, ShipmentJourneyEntity shipmentJourneyEntity) {
        OrderShipmentMetadata orderShipmentMetadata = orderShipmentMetadataService.createOrderShipmentMetaData(shipment, shipmentJourneyEntity);
        ShipmentEntity shipmentEntity = createShipmentHelper.createShipmentEntity(shipment, orderShipmentMetadata);
        shipmentEntity = shipmentRepository.save(shipmentEntity);
        shipment.setId(shipmentEntity.getId());
        shipment.getShipmentJourney().setJourneyId(shipmentEntity.getShipmentJourneyId());
        log.info(SHIPMENT_CREATED_FROM_ORDER, shipmentEntity.getId(), shipmentEntity.getOrder().getId(), shipmentEntity.getShipmentTrackingId());
        milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(shipment, MilestoneCode.OM_BOOKED);
        return shipment;
    }

    private ShipmentEntity updateShipment(final Shipment shipment, final ShipmentEntity entity) {
        if (!isMatchingShipment(shipment, entity)) {
            throw new ShipmentIdMismatchException(String.format(ERR_UPDATE_SHIPMENT_MISMATCH, entity.getShipmentTrackingId(),
                    entity.getOrder().getId(), entity.getOrganization().getId(), shipment.getShipmentTrackingId(),
                    shipment.getOrder().getId(), shipment.getOrganization().getId()));
        }
        updateShipmentHelper.updateEtaStatus(shipment);
        updateShipmentHelper.updateShipmentEntityFromDomain(shipment, entity);
        log.info(SHIPMENT_UPDATED, shipment.getId(), shipment.getOrder().getId(), shipment.getShipmentTrackingId());
        return shipmentRepository.save(entity);
    }

    public ShipmentJourneyEntity createShipmentJourneyEntity(Shipment shipment, boolean segmentsUpdated) {
        ShipmentJourney shipmentJourney = shipment.getShipmentJourney();
        Optional<ShipmentEntity> shipmentEntityOptional = shipmentFetchService.findShipmentByTrackingId(shipment.getShipmentTrackingId());
        Order order = shipment.getOrder();
        ShipmentJourneyEntity shipmentJourneyEntity = shipmentEntityOptional.map(shipmentEntity ->
                        shipmentJourneyService.update(shipmentJourney, shipmentEntity.getShipmentJourney(), order,
                                segmentsUpdated))
                .orElseGet(() -> shipmentJourneyService.create(shipmentJourney, order));

        packageJourneySegmentService.updateFacilityAndPartner(shipmentJourney.getPackageJourneySegments(),
                shipmentJourneyEntity.getPackageJourneySegments(), order.getPickupTimezone(), order.getDeliveryTimezone());
        return shipmentJourneyService.save(shipmentJourneyEntity);
    }

    @Transactional(readOnly = true)
    public Shipment findByIdAndCheckLocationPermission(String id) {
        ShipmentEntity entity = shipmentFetchService.findByIdWithFetchOrThrowException(id);
        shipmentEnrichmentService.enrichShipmentJourneyAndSegmentWithAlert(entity.getShipmentJourney());
        shipmentEnrichmentService.enrichShipmentPackageJourneySegmentsWithInstructions(entity.getShipmentJourney().getPackageJourneySegments());
        Shipment shipment = getShipmentWithLocationPermission(entity);
        String orderId = shipment.getOrder().getId();
        List<PackageJourneySegment> segments = shipment.getShipmentJourney().getPackageJourneySegments();
        packageJourneySegmentService.enrichSegmentsWithOrderInstructions(orderId, segments);
        return shipment;
    }

    @Transactional(readOnly = true)
    public Shipment findByShipmentTrackingIdAndCheckLocationPermission(String shipmentTrackingId) {
        ShipmentEntity entity = shipmentFetchService.findByShipmentTrackingIdOrThrowException(shipmentTrackingId);
        shipmentEnrichmentService.enrichShipmentJourneyAndSegmentWithAlert(entity.getShipmentJourney());
        shipmentEnrichmentService.enrichShipmentPackageJourneySegmentsWithInstructions(entity.getShipmentJourney().getPackageJourneySegments());
        Shipment shipment = getShipmentWithLocationPermission(entity);
        String orderId = shipment.getOrder().getId();
        List<PackageJourneySegment> segments = shipment.getShipmentJourney().getPackageJourneySegments();
        packageJourneySegmentService.enrichSegmentsWithOrderInstructions(orderId, segments);
        return shipment;
    }

    private void validateUniqueShipmentCreation(Shipment shipment) {
        if (shipmentRepository.isShipmentWithTrackingIdAndOrgIdExist(shipment.getShipmentTrackingId(), userDetailsProvider.getCurrentOrganizationId())) {
            throw new QuincusValidationException(String.format("Shipment with Tracking Id: %s already exist", shipment.getShipmentTrackingId()));
        }
    }

    private Shipment getShipmentWithLocationPermission(ShipmentEntity entity) {
        Shipment shipment = mapEntityToDomain(entity, objectMapper);
        if (!userDetailsProvider.isFromAllowedSource() && (!isShipmentAnySegmentLocationAllowed(entity) && !areAllFacilityIdsNull(entity))) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_READ_NOT_ALLOWED, shipment.getId()),
                    ShipmentErrorCode.RESOURCE_ACCESS_FORBIDDEN);
        }
        updateShipmentHelper.setupAddress(shipment.getShipmentJourney().getPackageJourneySegments());
        milestoneService.setMilestoneEventsForShipment(entity, shipment);
        return shipment;
    }

    @Utility
    @Transactional
    public Shipment createOrUpdateLocal(final Shipment shipment, final boolean segmentsUpdated) {
        shipment.setOrganization(userDetailsProvider.getCurrentOrganization());
        final Optional<ShipmentEntity> shipmentEntityOptional = shipmentRepository.findByShipmentTrackingIdAndOrgId(shipment.getShipmentTrackingId(), userDetailsProvider.getCurrentOrganizationId());
        ShipmentJourneyEntity shipmentJourneyEntity = createShipmentJourneyEntity(shipment, segmentsUpdated);
        return shipmentEntityOptional.map(shipmentEntity -> updateLocal(shipment, shipmentEntity)).orElseGet(() -> createShipment(shipment, shipmentJourneyEntity));
    }

    @Transactional
    public List<ShipmentResult> createBulk(List<Shipment> shipments) {
        List<ShipmentResult> shipmentResults = new ArrayList<>();
        for (Shipment shipment : shipments) {
            try {
                Order order = shipment.getOrder();

                shipment.setInternalOrderId(order.getOrderIdLabel());
                shipment.setOrganization(userDetailsProvider.getCurrentOrganization());
                ShipmentJourneyEntity shipmentJourneyEntity = createShipmentJourneyEntity(shipment, false);
                shipmentResults.add(new ShipmentResult(create(shipment, shipmentJourneyEntity), true));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                shipmentResults.add(new ShipmentResult(shipment, false));
            }
        }
        return shipmentResults;
    }

    @Transactional(readOnly = true)
    public List<Shipment> findActiveShipmentsWithAirSegment() {
        List<Tuple> shpObj = shipmentRepository.findActiveShipmentsPartialFieldsWithAirSegmentAndSegmentUncachedAndSegmentNoAlert();
        if (CollectionUtils.isEmpty(shpObj)) {
            return Collections.emptyList();
        }
        List<Shipment> shipments = new ArrayList<>();
        shpObj.forEach(o -> shipments.add(ShipmentUtil.convertObjectArrayToShipmentLimited(o)));

        List<PackageJourneySegment> segments = packageJourneySegmentService.getAllSegmentsFromShipments(shipments);
        ShipmentUtil.addSegmentsToShipments(shipments, segments);

        return shipments;
    }

    @Transactional(readOnly = true)
    public Shipment findShipmentFromTrackingIdForMilestoneBatch(String shipmentTrackingId, String organizationId) {
        return shipmentRepository.findShipmentActiveSegmentsOnlyByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId)
                .map(entity -> mapEntityToDomain(entity, objectMapper)).orElse(null);
    }

    @Transactional
    public void deleteById(String id) {
        shipmentFetchService.findByIdOrThrowException(id);
        shipmentRepository.deleteById(id);
        log.info(SHIPMENT_DELETED, id);
    }

    @Transactional
    public Shipment cancelById(String id, TriggeredFrom triggeredFrom) {
        Shipment cancelledShipment = cancelShipmentById(id);
        sendCancellationMessageToOtherModules(cancelledShipment, triggeredFrom);
        return cancelledShipment;
    }

    private void sendCancellationMessageToOtherModules(Shipment cancelledShipment, TriggeredFrom triggeredFrom) {
        qLoggerAPI.publishShipmentCancelledEvent(SHIPMENT_SERVICE_CANCEL_BY_ID, cancelledShipment);
        messageApi.sendMilestoneMessage(cancelledShipment, triggeredFrom);
        messageApi.sendSegmentDispatch(List.of(cancelledShipment), cancelledShipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CANCELLED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Transactional
    public Shipment cancelShipmentById(String id) {
        ShipmentEntity entity = shipmentFetchService.findByIdOrThrowException(id);
        Shipment shipmentToCancel = mapEntityToDomain(entity, objectMapper);
        if (!isShipmentAnySegmentLocationAllowed(entity)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_CANCEL_NOT_ALLOWED, shipmentToCancel.getId()),
                    ShipmentErrorCode.SHIPMENT_CANCEL_NOT_ALLOWED);
        }
        if (ShipmentStatus.CANCELLED == entity.getStatus()) {
            throw new ShipmentInvalidStatusException(String.format(ERR_SHIPMENT_ALREADY_CANCELLED, id));
        }
        entity.setStatus(ShipmentStatus.CANCELLED);
        cancelJourney(entity);
        entity.setEtaStatus(null);
        entity = shipmentRepository.save(entity);
        log.info(SHIPMENT_CANCELLED, entity.getId(), entity.getOrder().getId(), entity.getShipmentTrackingId());
        return mapEntityToDomain(entity, objectMapper);
    }

    private void cancelJourney(ShipmentEntity entity) {
        if (areRelatedShipmentsForJourneyCancelled(entity)) {
            entity.getShipmentJourney().setStatus(JourneyStatus.CANCELLED);
        }
    }

    private boolean areRelatedShipmentsForJourneyCancelled(ShipmentEntity shipmentEntity) {
        int nonCancelledRelatedShipmentCount = shipmentRepository.countShipmentJourneyIdAndIdNotAndStatusNot(
                shipmentEntity.getShipmentJourney().getId(), shipmentEntity.getId(), ShipmentStatus.CANCELLED);
        return nonCancelledRelatedShipmentCount == 0;
    }

    @Transactional(readOnly = true)
    public List<Shipment> findAllByIds(List<String> ids) {
        return shipmentRepository.findAllById(ids).stream().map(shipment -> mapEntityToDomain(shipment, objectMapper)).toList();
    }

    @Transactional(readOnly = true)
    public ShipmentFilterResult findAll(ShipmentFilter shipmentFilter) {
        ShipmentCriteria shipmentCriteria = shipmentCriteriaMapper.mapFilterToCriteria(
                shipmentFilter,
                objectMapper,
                shipmentLocationCoveragePredicates
        );
        shipmentCriteria.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        shipmentCriteria.setOrganization(userDetailsProvider.getCurrentOrganization());
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        ShipmentSpecification shipmentSpecification = shipmentCriteria.buildSpecification();
        if (MapUtils.isEmpty(shipmentCriteria.getUserLocationCoverageIdsByType())) {
            return createEmptyShipmentFilter(shipmentFilter);
        }
        userPartnerCriteriaEnricher.enrichCriteriaByPartners(shipmentCriteria);

        long resultCount = shipmentRepository.count(shipmentSpecification);
        Pageable page = shipmentSpecification.buildPageable();
        List<ShipmentEntity> result = projectionListingPage.findAllWithPagination(shipmentSpecification, page);
        List<Shipment> shipments = ShipmentMapper.mapEntitiesForListing(result);
        shipments.stream()
                .filter(shipment -> CollectionUtils.isNotEmpty(shipment.getMilestoneEvents()))
                .forEach(shipment -> shipment.setMilestone(shipment.getMilestoneEvents().get(0)));
        int currentPage = shipmentFilter.getPageNumber() + 1;
        shipmentFilter.setPageNumber(currentPage + 1);
        return createShipmentFilterResult(shipmentFilter, shipmentCriteria, resultCount, page, shipments);
    }

    private ShipmentFilterResult createShipmentFilterResult(ShipmentFilter shipmentFilter, ShipmentCriteria shipmentCriteria, long resultCount, Pageable page, List<Shipment> shipments) {
        return new ShipmentFilterResult(shipments)
                .filter(shipmentFilter)
                .totalElements(resultCount)
                .totalPages(projectionListingPage.getTotalNumberOfPages(resultCount, page.getPageSize()))
                .currentPage(shipmentCriteria.getPage());
    }

    private ShipmentFilterResult createEmptyShipmentFilter(ShipmentFilter shipmentFilter) {
        return new ShipmentFilterResult(List.of())
                .filter(shipmentFilter)
                .totalElements(0)
                .totalPages(0)
                .currentPage(1);
    }

    public boolean isRetryableToDispatch(String shipmentId) {
        log.info(INFO_CHECKING_RETRY_TO_DISPATCH_ALLOWED, shipmentId);
        boolean retryable = milestoneService.isRetryableToDispatch(shipmentId);
        if (!retryable) {
            ShipmentEntity shipmentEntity = shipmentFetchService.findByIdOrThrowException(shipmentId);
            log.warn(WARN_RETRY_TO_DISPATCH_NOT_ALLOWED);
            alertService.createPickupDeliveryFailedAlert(shipmentEntity.getShipmentJourney());
        }
        return retryable;
    }

    @Transactional
    public Milestone receiveMilestoneMessageFromDispatch(String dspPayload, String uuid) {
        try {
            Milestone milestone = milestoneService.convertAndValidateMilestoneFromDispatch(dspPayload, uuid);
            // Finding previousMilestoneTime should be before milestoneService.createMilestone or else this will just get the currently processed milestone
            final OffsetDateTime previousMilestoneTime = milestoneService.getMostRecentMilestoneTimeByShipmentId(milestone.getShipmentId());
            milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone);
            milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
            milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromHubTimezone(milestone);
            milestoneService.createMilestone(milestone);

            if (milestoneService.isNewMilestoneAfterMostRecentMilestone(milestone.getMilestoneTime(), previousMilestoneTime)
                    && milestoneService.isFailedStatusCode(milestone.getMilestoneCode())) {
                alertService.createPickupDeliveryFailedAlert(milestone.getShipmentId());
            }

            if (!milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)) {
                milestone.setSegmentUpdatedFromMilestone(false);
                return milestone;
            }

            milestoneService.updateMilestoneAndPackageJourneySegment(milestone, uuid, previousMilestoneTime);
            return milestone;
        } catch (ConstraintViolationException e) {
            MilestoneError milestoneError = new MilestoneError();
            milestoneError.setMilestone(milestoneService.getDispatchMessageJson(dspPayload));
            List<String> errorList = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();
            milestoneError.setErrors(errorList);
            messageApi.sendDispatchMilestoneError(milestoneError);
        } catch (Exception e) {
            MilestoneError milestoneError = new MilestoneError();
            milestoneError.setMilestone(milestoneService.getDispatchMessageJson(dspPayload));
            milestoneError.setErrors(List.of(e.getMessage()));
            messageApi.sendDispatchMilestoneError(milestoneError);
        }
        return null;
    }

    @Transactional
    public ShipmentMilestoneOpsUpdateResponse updateShipmentFromOpsUpdate(ShipmentMilestoneOpsUpdateRequest infoRequest) {
        ShipmentEntity shipmentEntity = validateMilestoneInfoAndPrepareShipment(infoRequest);
        PackageJourneySegmentEntity currentActiveSegment = ShipmentUtil.getActiveSegmentEntity(shipmentEntity);
        Milestone previousMilestone = milestoneService.getLatestMilestone(shipmentEntity.getMilestoneEvents());
        OffsetDateTime previousMilestoneTime = previousMilestone.getMilestoneTime();
        Milestone currentMilestone = milestoneService.createMilestoneFromOpsUpdate(shipmentEntity, currentActiveSegment, infoRequest);
        Shipment updatedShipment = mapEntityToDomain(shipmentEntity, objectMapper);

        if (milestoneService.isNewMilestoneAfterMostRecentMilestone(currentMilestone.getMilestoneTime(), previousMilestoneTime)
                && milestoneService.isFailedStatusCode(currentMilestone.getMilestoneCode())) {
            alertService.createPickupDeliveryFailedAlert(shipmentEntity.getShipmentJourney());
        }

        sendMilestoneUpdates(updatedShipment, currentActiveSegment, currentMilestone, infoRequest);
        if (!milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(currentMilestone)) {
            currentMilestone.setSegmentUpdatedFromMilestone(false);
        } else {
            milestoneService.updateMilestoneAndPackageJourneySegment(currentMilestone, getTransactionId(), previousMilestoneTime);
        }

        if (currentMilestone.isSegmentUpdatedFromMilestone() && nonNull(currentActiveSegment)) {
            packageJourneySegmentService.refreshJourneyWithUpdatedSegments(updatedShipment.getShipmentJourney());
            shipmentPostProcessService.sendUpdatedSegmentToDispatch(currentMilestone, updatedShipment);
            shipmentPostProcessService.sendSingleSegmentToQship(updatedShipment, currentActiveSegment.getId());
            milestonePostProcessService.createAndSendAPIGWebhooks(currentMilestone, updatedShipment);
        }
        shipmentPostProcessService.publishShipmentUpdatedEvent(updatedShipment);
        notificationApi.sendNotification(NotificationRequest.ofMilestoneNotification(updatedShipment, currentMilestone, updatedShipment.getOrganization().getId()));
        return ShipmentMapper.toShipmentMilestoneOpsUpdateResponse(infoRequest, previousMilestone, currentMilestone, updatedShipment);
    }

    public ShipmentEntity validateMilestoneInfoAndPrepareShipment(ShipmentMilestoneOpsUpdateRequest infoRequest) {
        ShipmentEntity shipmentEntity = shipmentFetchService.findByShipmentTrackingIdOrThrowException(infoRequest.getShipmentTrackingId());

        if (updateShipmentHelper.isShipmentCancelled(shipmentEntity)) {
            throw new OperationNotAllowedException(UPDATE_ON_CANCELLED_SHIPMENT_NOT_ALLOWED);
        }

        if (!isShipmentAnySegmentLocationAllowed(shipmentEntity)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_MILESTONE_OTHER_INFO_NOT_ALLOWED, shipmentEntity.getId()),
                    ShipmentErrorCode.MILESTONE_INFO_UPDATE_NOT_ALLOWED);
        }
        return shipmentEntity;
    }

    private boolean areAllFacilityIdsNull(@NonNull final ShipmentEntity shipmentEntity) {
        final Optional<ShipmentJourneyEntity> journeyEntity = Optional.ofNullable(shipmentEntity.getShipmentJourney());
        return journeyEntity.map(shipmentJourneyEntity -> shipmentJourneyEntity.getPackageJourneySegments().stream()
                .noneMatch(segment -> segment.getStartLocationHierarchy() != null
                        || segment.getEndLocationHierarchy() != null)).orElse(false);
    }

    public boolean isShipmentAnySegmentLocationAllowed(@NonNull final ShipmentEntity shipmentEntity) {
        if (userDetailsProvider.isFromAllowedSource()) {
            return true;
        }
        ShipmentJourneyEntity journey = shipmentEntity.getShipmentJourney();
        if (journey == null || CollectionUtils.isEmpty(journey.getPackageJourneySegments())) {
            return false;
        }

        return journey.getPackageJourneySegments().stream()
                .anyMatch(packageJourneySegmentService::isSegmentAllFacilitiesAllowed);
    }

    public Map<String, List<PackageJourneySegment>> findActiveAirSegmentsMap(String shipmentTrackingId, String organizationId) {
        Map<String, List<PackageJourneySegment>> earliestActiveAirSegmentsMap = new HashMap<>();
        List<Tuple> packageJourneySegmentTupleList = shipmentRepository.findRelatedShipmentActiveAirSegmentsByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId);
        for (Tuple tuple : packageJourneySegmentTupleList) {
            String shipmentId = tuple.get(BaseEntity_.ID, String.class);
            PackageJourneySegment segment = new PackageJourneySegment();
            segment.setSegmentId(tuple.get(ShipmentJourneyEntity_.PACKAGE_JOURNEY_SEGMENTS, String.class));
            segment.setRefId(tuple.get(PackageJourneySegmentEntity_.REF_ID, String.class));
            segment.setStatus(tuple.get(PackageJourneySegmentEntity_.STATUS, SegmentStatus.class));
            segment.setSequence(tuple.get(PackageJourneySegmentEntity_.SEQUENCE, String.class));
            segment.setDepartureTimezone(tuple.get(PackageJourneySegmentEntity_.DEPARTURE_TIMEZONE, String.class));
            segment.setArrivalTimezone(tuple.get(PackageJourneySegmentEntity_.ARRIVAL_TIMEZONE, String.class));
            segment.setStartFacility(mapTupleToFacility(tuple, "start"));
            segment.setEndFacility(mapTupleToFacility(tuple, "end"));
            earliestActiveAirSegmentsMap.computeIfAbsent(shipmentId, key -> new ArrayList<>()).add(segment);
        }
        return earliestActiveAirSegmentsMap;
    }

    private Facility mapTupleToFacility(Tuple tuple, String prefix) {
        Facility facility = new Facility();
        facility.setId(tuple.get(prefix.concat("FacilityId"), String.class));
        facility.setName(tuple.get(prefix.concat("FacilityName"), String.class));

        Address address = new Address();
        address.setCountryId(tuple.get(prefix.concat("CountryExternalId"), String.class));
        address.setStateId(tuple.get(prefix.concat("StateExternalId"), String.class));
        address.setCityId(tuple.get(prefix.concat("CityExternalId"), String.class));
        address.setCountryName(tuple.get(prefix.concat("CountryName"), String.class));
        address.setStateName(tuple.get(prefix.concat("StateName"), String.class));
        address.setCityName(tuple.get(prefix.concat("CityName"), String.class));
        facility.setLocation(address);
        facility.setTimezone(tuple.get(prefix.concat("FacilityTimezone"), String.class));
        return facility;
    }

    private void sendMilestoneUpdates(Shipment shipment, PackageJourneySegmentEntity activeSegmentEntity,
                                      Milestone milestone, ShipmentMilestoneOpsUpdateRequest milestoneOpsUpdateRequest) {
        if (isNull(milestone) || isNull(milestone.getMilestoneCode())) {
            log.warn(ERR_MILESTONE_NULL);
            return;
        }
        shipment.setMilestone(milestone);
        Milestone milestoneFromShp = shipment.getMilestone();
        if (nonNull(activeSegmentEntity)) {
            if (milestoneFromShp.getAdditionalInfo() == null) {
                milestoneFromShp.setAdditionalInfo(new MilestoneAdditionalInfo());
            }
            milestoneFromShp.getAdditionalInfo().setRemarks(milestoneOpsUpdateRequest.getNotes());
            milestoneFromShp.setSegmentId(activeSegmentEntity.getId());
        }
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);
    }

    private void sendUpdatesToQlogger(Shipment shipment, ShipmentEntity previousShipmentEntity, Shipment updatedShipment) {
        Shipment previousShipment = mapEntityToDomain(previousShipmentEntity, objectMapper);
        shipmentPostProcessService.publishQLoggerUpdateEvents(shipment, updatedShipment, previousShipment);
    }

    private void handleCancelledOrder(Shipment shipment, ShipmentEntity entity) {
        if (!updateShipmentHelper.isShipmentCancelled(entity)) {
            cancelAllRelatedShipmentsByOrderId(shipment.getOrder().getId(), shipment.getOrder().getCancelReason());
            return;
        }
        if (shipment.isSegmentsUpdatedFromSource()) {
            ShipmentJourney shipmentJourney = shipment.getShipmentJourney();
            shipmentJourney.setShipmentId(shipment.getId());
            shipmentJourney.setOrderId(shipment.getOrder().getId());
            shipmentJourney.setJourneyId(entity.getShipmentJourneyId());
            packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourney);
            packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, true);
            shipmentJourneyService.validateAndUpdateShipmentJourney(shipmentJourney);
            cancelSegments(entity, entity.getOrder().getCancelReason());
        }
    }

    private void handlePackageDimensionUpdate(ShipmentEntity previousShipment, Shipment updatedShipment) {
        boolean isPackageDimensionUpdated = packageDimensionService.isPackageDimensionUpdated(previousShipment.getShipmentPackage().getDimension(),
                updatedShipment.getShipmentPackage().getDimension());
        if (isPackageDimensionUpdated) {
            Milestone milestone = milestoneService.createPackageDimensionUpdateMilestone(updatedShipment);
            updatedShipment.setMilestone(milestone);
            updatedShipment.setMilestoneEvents(List.of(milestone));
            messageApi.sendMilestoneMessage(updatedShipment, TriggeredFrom.OM);
            packageLogService.upsertPackageLogForShipmentPackage(updatedShipment.getId(), updatedShipment.getShipmentPackage());
        }
    }

    private void cancelAllRelatedShipmentsByOrderId(String orderId, String cancelReason) {
        final List<ShipmentEntity> shipmentEntities = shipmentRepository.findAllByOrderId(orderId, userDetailsProvider.getCurrentOrganizationId());
        if (CollectionUtils.isEmpty(shipmentEntities)) return;
        shipmentEntities.forEach(shipmentEntity -> {
            shipmentEntity.setStatus(ShipmentStatus.CANCELLED);
            OrderEntity order = shipmentEntity.getOrder();
            order.setStatus(ShipmentStatus.CANCELLED.name());
            order.setCancelReason(cancelReason);
            cancelSegments(shipmentEntity, cancelReason);
            Shipment shipment = mapEntityToDomain(shipmentEntity, objectMapper);
            shipmentRepository.save(shipmentEntity);
            milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(shipment, MilestoneCode.OM_ORDER_CANCELED);
            messageApi.sendSegmentDispatch(List.of(shipment), shipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CANCELLED, DspSegmentMsgUpdateSource.CLIENT);
            qLoggerAPI.publishShipmentCancelledEvent(SHIPMENT_SERVICE_CANCEL_BY_ID, shipment);
            log.info(SHIPMENT_CANCELLED, shipmentEntity.getId(), order.getId(), shipmentEntity.getShipmentTrackingId());
        });
    }

    private void cancelSegments(ShipmentEntity shipmentEntity, String reason) {
        List<PackageJourneySegmentEntity> segmentEntities = shipmentEntity.getShipmentJourney().getPackageJourneySegments();
        if (CollectionUtils.isEmpty(segmentEntities)) return;
        Shipment shipment = mapEntityToDomain(shipmentEntity, objectMapper);
        List<PackageJourneySegmentEntity> notCompletedSegments = segmentEntities.stream().filter(s -> !SegmentStatus.COMPLETED.equals(s.getStatus())).toList();
        notCompletedSegments.forEach(segment -> {
            segment.getShipmentJourney().setStatus(JourneyStatus.CANCELLED);
            segment.setStatus(SegmentStatus.CANCELLED);
            packageJourneySegmentRepository.save(segment);
            messageApi.sendSegmentCancelled(shipment, segment.getId(), reason);
            messageApi.sendUpdatedSegmentFromShipment(shipment, segment.getId());
        });
    }

    @Utility
    private Shipment updateLocal(
            final Shipment shipment,
            final ShipmentEntity entity
    ) {
        final Shipment updatedShipment = mapEntityToDomain(updateShipment(shipment, entity), objectMapper);
        shipment.setSegmentUpdated(isJourneyOrSegmentUpdated(shipment, updatedShipment));
        return updatedShipment;
    }

    private boolean isMatchingShipment(Shipment shipment, ShipmentEntity entity) {
        return (shipment.getShipmentTrackingId().equalsIgnoreCase(entity.getShipmentTrackingId()))
                && (shipment.getOrder().getId().equalsIgnoreCase(entity.getOrder().getId()))
                && (shipment.getOrganization().getId().equalsIgnoreCase(entity.getOrganization().getId()));
    }

    private boolean isJourneyOrSegmentUpdated(Shipment baseShipment, Shipment updatedShipment) {
        ShipmentJourney baseJourney = baseShipment.getShipmentJourney();
        ShipmentJourney updatedJourney = updatedShipment.getShipmentJourney();
        if (!updatedJourney.getJourneyId().equals(baseJourney.getJourneyId())) {
            return true;
        }

        //In case of segments updates, the whole list will be replaced by a new set. Checking the 1st element is enough.
        PackageJourneySegment baseSegment = baseJourney.getPackageJourneySegments().get(0);
        PackageJourneySegment updatedSegment = updatedJourney.getPackageJourneySegments().get(0);
        return updatedSegment.getSegmentId().equals(baseSegment.getSegmentId());
    }

    private void identifyAndUpdateShipments(List<ShipmentEntity> existingShipments,
                                            ShipmentJourneyEntity shipmentJourney,
                                            List<ShipmentEntity> missingShipments,
                                            List<Shipment> newShipments,
                                            List<Shipment> updatedShipments) {
        for (ShipmentEntity existingShipment : existingShipments) {
            missingShipments.add(existingShipment);
            for (Iterator<Shipment> iterator = newShipments.iterator(); iterator.hasNext(); ) {
                Shipment shipment = iterator.next();
                if (StringUtils.equalsIgnoreCase(existingShipment.getShipmentTrackingId(), shipment.getShipmentTrackingId())) {
                    missingShipments.remove(existingShipment);
                    updatedShipments.add(update(shipment, existingShipment, shipmentJourney));
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private void createByOrderShipmentMetadata(Shipment shipment, OrderShipmentMetadata orderShipmentMetadata) {
        shipment.setOrganization(userDetailsProvider.getCurrentOrganization());
        shipment.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        ShipmentEntity shipmentEntity = shipmentRepository.save(createShipmentHelper.createShipmentEntity(shipment, orderShipmentMetadata));
        shipment.setId(shipmentEntity.getId());
        //Mapper is necessary for Segments so updates on timezones are reflected
        shipment.setShipmentJourney(ShipmentJourneyMapper.mapEntityToDomain(shipmentEntity.getShipmentJourney()));
        syncUpdatedAddressEntityToDomain(shipment.getOrigin(), shipmentEntity.getOrigin());
        syncUpdatedAddressEntityToDomain(shipment.getDestination(), shipmentEntity.getDestination());
        shipment.getShipmentPackage().setId(shipmentEntity.getShipmentPackage().getId());
        shipment.getServiceType().setId(shipmentEntity.getServiceType().getId());
        log.info(SHIPMENT_CREATED_FROM_ORDER, shipmentEntity.getId(), shipmentEntity.getOrder().getId(), shipmentEntity.getShipmentTrackingId());
        milestoneService.createOMTriggeredMilestoneAndSendMilestoneMessage(shipment, MilestoneCode.OM_BOOKED);
        packageLogService.createPackageLogForShipmentPackage(shipment.getId(), shipment.getShipmentPackage());
        shipmentPostProcessService.publishQloggerCreateEvents(shipment);
    }

    private void syncUpdatedAddressEntityToDomain(Address address, AddressEntity addressEntity) {
        address.setId(addressEntity.getId());
        address.setExternalId(addressEntity.getExternalId());
    }

    private void setJourneyAndSegmentIds(ShipmentJourney journey, ShipmentJourneyEntity journeyEntity) {
        journey.setJourneyId(journeyEntity.getId());
        for (PackageJourneySegment segment : journey.getPackageJourneySegments()) {
            segment.setJourneyId(journeyEntity.getId());
            segmentUpdateChecker.findSegmentForUpdate(segment, journeyEntity.getPackageJourneySegments())
                    .ifPresent(s -> segment.setSegmentId(s.getId()));
        }
    }

    private void adjustSegmentFromMissingShipments(List<ShipmentEntity> missingShipments, ShipmentJourneyEntity refJourney) {
        if (CollectionUtils.isNotEmpty(missingShipments)) {
            List<String> missingShipmentIds = missingShipments.stream().map(BaseEntity::getId).toList();
            milestoneService.getMilestoneWithPendingSegmentUpdate(missingShipmentIds)
                    .ifPresent(milestone ->
                            refJourney.getPackageJourneySegments().stream()
                                    .filter(segmentEntity -> milestone.getSegmentId().equals(segmentEntity.getId()))
                                    .findFirst()
                                    .ifPresent(segmentEntity ->
                                            packageJourneySegmentService.updateSegmentByMilestone(segmentEntity, milestone, true)
                                    )
                    );
        }
    }

    private Shipment getRefShipment(final List<Shipment> shipments, List<ShipmentEntity> existingShipments) {
        if (CollectionUtils.isEmpty(existingShipments)) {
            return shipments.get(0);
        }

        Set<String> existingShipmentTrackingId = existingShipments.stream()
                .map(ShipmentEntity::getShipmentTrackingId).collect(Collectors.toSet());

        return shipments.stream().filter(s -> existingShipmentTrackingId.contains(s.getShipmentTrackingId()))
                .findFirst().orElse(shipments.get(0));
    }

}
