package com.quincus.shipment.impl.service;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.Vehicle;
import com.quincus.shipment.api.exception.SegmentNotFoundException;
import com.quincus.shipment.api.helper.MilestoneCodeUtil;
import com.quincus.shipment.impl.helper.InstructionUtil;
import com.quincus.shipment.impl.helper.SegmentReferenceHolder;
import com.quincus.shipment.impl.helper.SegmentReferenceProvider;
import com.quincus.shipment.impl.helper.segment.SegmentUpdateChecker;
import com.quincus.shipment.impl.mapper.DriverMapper;
import com.quincus.shipment.impl.mapper.InstructionMapper;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.VehicleMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.SegmentLockoutTimePassedRepository;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.SegmentLockoutTimePassedEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.quincus.ext.DateTimeUtil.getOffset;
import static com.quincus.shipment.impl.helper.DispatchModuleUtil.CANCELLED_STATUS_CODES_FROM_DISPATCH;
import static com.quincus.shipment.impl.helper.DispatchModuleUtil.COMPLETED_STATUS_CODES_FROM_DISPATCH;
import static com.quincus.shipment.impl.helper.DispatchModuleUtil.IN_PROGRESS_STATUS_CODES_FROM_DISPATCH;
import static com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper.createFacility;
import static com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper.getNameBySegmentType;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PackageJourneySegmentService {
    static final DateTimeFormatter ZONED_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
    private static final InstructionMapper INSTRUCTION_MAPPER = Mappers.getMapper(InstructionMapper.class);
    private static final String INFO_UPDATING_SEGMENT_STATUS_BY_MILESTONE_EVENT = "Updating segment status of based on milestoneEvent: {}. UUID: {}, Segment Id {} ";
    private static final String ERR_SEGMENT_NOT_FOUND = "Segment with Id: %s not found. UUID: %s ";
    private static final String WARN_UNRECOGNIZED_STATUS_CODE = "Unrecognized status code: {} from Dispatch message. Segment status will not be updated. UUID: {}";
    private static final String UTC = "((?=UTC))";
    private final PackageJourneySegmentRepository packageJourneySegmentRepository;
    private final SegmentLockoutTimePassedRepository segmentLockoutTimePassedRepository;
    private final UserDetailsProvider userDetailsProvider;
    private final FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final FacilityService facilityService;
    private final AddressService addressService;
    private final LocationHierarchyService locationHierarchyService;
    private final SegmentReferenceProvider segmentReferenceProvider;
    private final PartnerService partnerService;
    private final InstructionFetchService instructionFetchService;
    private final SegmentUpdateChecker segmentUpdateChecker;

    public List<PackageJourneySegmentEntity> findByShipmentJourneyIds(final List<String> ids) {
        List<Tuple> packageJourneySegmentTuples = packageJourneySegmentRepository.findByShipmentJourneyIdIn(ids);
        return packageJourneySegmentTuples.stream()
                .map(PackageJourneySegmentMapper::mapTupleToEntity)
                .toList();
    }

    @Transactional
    public boolean updateSegmentStatusByMilestoneEvent(final Milestone milestone, final String uuid) {
        log.debug(INFO_UPDATING_SEGMENT_STATUS_BY_MILESTONE_EVENT, uuid, milestone.getSegmentId(), milestone);

        SegmentStatus status = getSegmentStatusByMilestoneEventCode(milestone.getMilestoneCode());
        if (status == null) {
            log.warn(WARN_UNRECOGNIZED_STATUS_CODE, milestone.getMilestoneCode(), uuid);
            return false;
        }

        PackageJourneySegmentEntity segmentEntity = packageJourneySegmentRepository.findById(milestone.getSegmentId())
                .orElseThrow(() -> new SegmentNotFoundException(String.format(ERR_SEGMENT_NOT_FOUND, milestone.getSegmentId(), uuid), uuid));
        segmentEntity.setStatus(status);
        packageJourneySegmentRepository.save(segmentEntity);
        return true;
    }

    public boolean updateSegmentStatusByMilestone(final PackageJourneySegmentEntity segmentEntity,
                                                  final Milestone milestone) {
        SegmentStatus status = getSegmentStatusByMilestoneEventCode(milestone.getMilestoneCode());
        if ((status == null) || status == segmentEntity.getStatus()) {
            return false;
        }
        segmentEntity.setStatus(status);
        return true;
    }

    @Transactional
    public void updateFacilityAndPartner(final List<PackageJourneySegment> packageJourneySegments
            , final List<PackageJourneySegmentEntity> packageJourneySegmentEntityList, final String orderPickupTimeZone
            , final String deliveryTimeZone) {

        if (CollectionUtils.isEmpty(packageJourneySegments) || CollectionUtils.isEmpty(packageJourneySegmentEntityList)) {
            return;
        }

        SegmentReferenceHolder segmentReferenceHolder = segmentReferenceProvider.generateReference(packageJourneySegments);
        packageJourneySegments.forEach(segment -> {
            PackageJourneySegmentEntity entitySegment = packageJourneySegmentEntityList.stream()
                    .filter(e -> segmentUpdateChecker.isSegmentMatch(segment, e)).findFirst().orElse(null);
            if (entitySegment == null) return;
            assignPartnerToSegmentEntityFromReferenceOrCreate(segmentReferenceHolder.getPartnerBySegmentId(), segment.getPartner(), entitySegment);
            enrichSegmentFacilities(segment);
            assignLocationHierarchyFromReferenceOrCreated(segmentReferenceHolder.getLocationHierarchyByFacilityExtId(), segment, entitySegment);
            createOrUpdateAddressForStartAndEndFacility(segment, entitySegment);
            facilityService.setupFlightOriginAndDestination(segment, entitySegment);
            setUnitLabels(segment);
            setTimezones(orderPickupTimeZone, deliveryTimeZone, entitySegment);
            entitySegment.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        });
    }

    private void setUnitLabels(PackageJourneySegment packageJourneySegment) {
        UnitOfMeasure calculatedMileageUnit = packageJourneySegment.getCalculatedMileageUnit();
        UnitOfMeasure durationUnit = packageJourneySegment.getDurationUnit();
        packageJourneySegment.setCalculatedMileageUnitLabel(calculatedMileageUnit == null ? null : calculatedMileageUnit.getLabel());
        packageJourneySegment.setDurationUnitLabel(durationUnit == null ? null : durationUnit.getLabel());
    }

    private void enrichSegmentFacilities(PackageJourneySegment packageJourneySegments) {
        facilityService.enrichFacilityWithLocationFromQPortal(packageJourneySegments.getStartFacility());
        facilityService.enrichFacilityWithLocationFromQPortal(packageJourneySegments.getEndFacility());
    }

    public PackageJourneySegmentEntity initializeLocationHierarchiesFromFacilities(SegmentReferenceHolder segmentReferenceHolder,
                                                                                   PackageJourneySegmentEntity segmentEntity,
                                                                                   PackageJourneySegment segment,
                                                                                   final String orderPickupTimeZone,
                                                                                   final String deliveryTimeZone) {
        if (segmentEntity == null || segment == null) {
            return segmentEntity;
        }

        assignPartnerToSegmentEntityFromReferenceOrCreate(segmentReferenceHolder.getPartnerBySegmentId(), segment.getPartner(), segmentEntity);
        enrichSegmentFacilities(segment);
        assignLocationHierarchyFromReferenceOrCreated(segmentReferenceHolder.getLocationHierarchyByFacilityExtId(), segment, segmentEntity);
        createOrUpdateAddressForStartAndEndFacility(segment, segmentEntity);
        facilityService.setupFlightOriginAndDestination(segment, segmentEntity);
        setTimezones(orderPickupTimeZone, deliveryTimeZone, segmentEntity);
        segmentEntity.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());

        return segmentEntity;
    }

    private void assignLocationHierarchyFromReferenceOrCreated(Map<String, LocationHierarchyEntity> locationHierarchyByFacilityExternalIds,
                                                               PackageJourneySegment packageJourneySegment, PackageJourneySegmentEntity packageJourneySegmentEntity) {

        packageJourneySegmentEntity.setStartLocationHierarchy(findFromReferenceOrCreateAndAddToReference(
                locationHierarchyByFacilityExternalIds, packageJourneySegment.getStartFacility()));

        packageJourneySegmentEntity.setEndLocationHierarchy(findFromReferenceOrCreateAndAddToReference(
                locationHierarchyByFacilityExternalIds, packageJourneySegment.getEndFacility()));

    }

    private void createOrUpdateAddressForStartAndEndFacility(PackageJourneySegment packageJourneySegment, PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegment.getStartFacility() != null) {
            addressService.saveAddress(packageJourneySegment.getStartFacility().getLocation()
                    , packageJourneySegmentEntity.getStartLocationHierarchy());
        }
        if (packageJourneySegment.getEndFacility() != null) {
            addressService.saveAddress(packageJourneySegment.getEndFacility().getLocation()
                    , packageJourneySegmentEntity.getEndLocationHierarchy());
        }

    }

    private LocationHierarchyEntity findFromReferenceOrCreateAndAddToReference(Map<String, LocationHierarchyEntity> locationHierarchyByFacilityExtIds
            , Facility facility) {
        if (facility == null) {
            return null;
        }
        String locationHierarchyUniqueReferenceIdentifier = generateLocationHierarchyUniqueKeyFromFacility(facility);
        if (locationHierarchyByFacilityExtIds.containsKey(locationHierarchyUniqueReferenceIdentifier)) {
            return locationHierarchyByFacilityExtIds.get(locationHierarchyUniqueReferenceIdentifier);
        }
        LocationHierarchyEntity locationHierarchyEntity = locationHierarchyService.setUpLocationHierarchyFromFacilityAndSave(facility);
        if (locationHierarchyEntity == null) {
            throw new QuincusException(String.format("There was an error on saving facility %s", facility.getExternalId()));
        }
        locationHierarchyByFacilityExtIds.put(locationHierarchyUniqueReferenceIdentifier, locationHierarchyEntity);
        return locationHierarchyEntity;
    }

    private String generateLocationHierarchyUniqueKeyFromFacility(Facility facility) {
        return facility.getLocation().getCountryId() + facility.getLocation().getStateId()
                + facility.getLocation().getCityId() + facility.getExternalId();
    }

    private void assignPartnerToSegmentEntityFromReferenceOrCreate(Map<String, PartnerEntity> partnerReferenceByExternalId, Partner partner, PackageJourneySegmentEntity entitySegment) {
        if (partner == null || StringUtils.isBlank(partner.getId())) return;

        if (partnerReferenceByExternalId.containsKey(partner.getId())) {
            entitySegment.setPartner(partnerReferenceByExternalId.get(partner.getId()));
            return;
        }
        entitySegment.setPartner(partnerService.createAndSavePartnerFromQPortal(partner.getId()));
        partnerReferenceByExternalId.put(entitySegment.getPartner().getExternalId(), entitySegment.getPartner());
    }

    public void setTimezones(String pickupTimezone, String deliveryTimezone, PackageJourneySegmentEntity segmentEntity) {
        if (segmentEntity == null) return;
        String startFacilityTimezone = getTimezone(segmentEntity.getStartLocationHierarchy(), pickupTimezone);
        String endFacilityTimezone = getTimezone(segmentEntity.getEndLocationHierarchy(), deliveryTimezone);
        if (TransportType.GROUND == segmentEntity.getTransportType()) {
            segmentEntity.setPickUpTimezone(getOffset(segmentEntity.getPickUpTime(), startFacilityTimezone));
            segmentEntity.setDropOffTimezone(getOffset(segmentEntity.getDropOffTime(), endFacilityTimezone));
            segmentEntity.setPickUpCommitTimezone(getOffset(segmentEntity.getPickUpCommitTime(), startFacilityTimezone));
            segmentEntity.setDropOffCommitTimezone(getOffset(segmentEntity.getDropOffCommitTime(), endFacilityTimezone));
            segmentEntity.setPickUpActualTimezone(getOffset(segmentEntity.getPickUpActualTime(), startFacilityTimezone));
            segmentEntity.setDropOffActualTimezone(getOffset(segmentEntity.getDropOffTime(), endFacilityTimezone));
            return;
        }
        segmentEntity.setDepartureTimezone(getOffset(segmentEntity.getDepartureTime(), startFacilityTimezone));
        segmentEntity.setArrivalTimezone(getOffset(segmentEntity.getArrivalTime(), endFacilityTimezone));
        segmentEntity.setLockOutTimezone(getOffset(segmentEntity.getLockOutTime(), startFacilityTimezone));
        segmentEntity.setRecoveryTimezone(getOffset(segmentEntity.getRecoveryTime(), endFacilityTimezone));
    }

    private String getTimezone(LocationHierarchyEntity locationHierarchyEntity, String defaultTimeZone) {
        if (locationHierarchyEntity == null) return defaultTimeZone;
        if (locationHierarchyEntity.getFacility() == null) {
            return locationHierarchyEntity.getCity() == null ? defaultTimeZone : locationHierarchyEntity.getCity().getTimezone();
        }
        String timezone = locationHierarchyEntity.getFacility().getTimezone();
        return timezone == null ? defaultTimeZone : timezone;
    }

    private SegmentStatus getSegmentStatusByMilestoneEventCode(MilestoneCode code) {
        if (IN_PROGRESS_STATUS_CODES_FROM_DISPATCH.contains(code)) {
            return SegmentStatus.IN_PROGRESS;
        } else if (COMPLETED_STATUS_CODES_FROM_DISPATCH.contains(code)) {
            return SegmentStatus.COMPLETED;
        } else if (CANCELLED_STATUS_CODES_FROM_DISPATCH.contains(code)) {
            return SegmentStatus.CANCELLED;
        }
        return null;
    }

    public List<PackageJourneySegmentEntity> findSegmentsWithFlightDetails(String airlineCode, String flightNumber, String departureDate,
                                                                           String origin, String destination, Long flightId) {
        return packageJourneySegmentRepository.findSegmentsWithFlightDetails(airlineCode, flightNumber, departureDate, origin, destination, flightId);
    }

    public List<PackageJourneySegmentEntity> findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(String airlineCode, String flightNumber, String departureDate,
                                                                                                                   String origin, String destination) {
        return packageJourneySegmentRepository.findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(airlineCode, flightNumber, departureDate, origin, destination);
    }

    @Transactional
    public void save(PackageJourneySegment segment) {
        packageJourneySegmentRepository.save(PackageJourneySegmentMapper.mapDomainToEntity(segment));
    }

    public PackageJourneySegment save(PackageJourneySegmentEntity segmentEntity) {
        return PackageJourneySegmentMapper.mapEntityToDomain(packageJourneySegmentRepository.save(segmentEntity));
    }

    @Transactional
    public void update(PackageJourneySegmentEntity segment) {
        packageJourneySegmentRepository.save(segment);
    }

    public boolean isSegmentLockoutTimeMissed(PackageJourneySegment segment) {
        ZonedDateTime lockoutTime = DateTimeUtil.parseZonedDateTime(segment.getLockOutTime());
        if (lockoutTime == null) {
            return false;
        }
        ZonedDateTime sysZonedLockoutTime = lockoutTime.withZoneSameInstant(ZoneId.systemDefault());
        LocalDateTime sysLocalLockoutTime = sysZonedLockoutTime.toLocalDateTime();

        LocalDateTime currentTime = LocalDateTime.now();

        return sysLocalLockoutTime.isBefore(currentTime);
    }

    public List<PackageJourneySegment> getAllSegmentsFromShipments(List<Shipment> shipments) {
        if (CollectionUtils.isEmpty(shipments)) {
            return Collections.emptyList();
        }
        List<String> shipmentIdList = shipments.stream().map(Shipment::getId).distinct().toList();
        return packageJourneySegmentRepository.findAllSegmentsFromAllShipmentIds(shipmentIdList)
                .stream()
                .map(this::toPackageJourneySegment)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cacheLockoutTimePassedSegment(PackageJourneySegment segment) {
        SegmentLockoutTimePassedEntity entity = new SegmentLockoutTimePassedEntity();
        entity.setSegmentId(segment.getSegmentId());
        segmentLockoutTimePassedRepository.save(entity);

        log.debug("Cached segment {}. Reason: Lockout time passed.", segment.getSegmentId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unCacheLockoutTimePassedSegment(String segmentId) {
        segmentLockoutTimePassedRepository.deleteBySegmentId(segmentId);
        log.debug("Un-cached segment {}.", segmentId);
    }

    public Optional<PackageJourneySegmentEntity> findBySegmentId(final String segmentId) {
        return packageJourneySegmentRepository.findById(segmentId);
    }

    public void refreshJourneyWithUpdatedSegments(ShipmentJourney shipmentJourney) {
        Optional.ofNullable(shipmentJourney).ifPresent(sj -> {
            String organizationId = userDetailsProvider.getCurrentOrganizationId();
            assignPackageJourneySegments(sj, organizationId);
        });
    }

    private void assignPackageJourneySegments(ShipmentJourney shipmentJourney, String organizationId) {
        List<PackageJourneySegmentEntity> packageJourneySegmentEntities =
                packageJourneySegmentRepository.findByShipmentJourneyIdAndOrganizationId(shipmentJourney.getJourneyId(), organizationId);
        shipmentJourney.setPackageJourneySegments(
                PackageJourneySegmentMapper.mapEntityListToDomainListPackageJourneySegment(packageJourneySegmentEntities));
    }

    public boolean isSegmentAllFacilitiesAllowed(@NonNull final PackageJourneySegment segment) {
        return isFacilityAllowed(segment.getStartFacility()) || isFacilityAllowed(segment.getEndFacility());
    }

    public boolean isSegmentAllFacilitiesAllowed(@NonNull final PackageJourneySegmentEntity segmentEntity) {
        Facility startFacility = createFacility(segmentEntity.getStartLocationHierarchy(), getNameBySegmentType(segmentEntity.getType()));
        Facility endFacility = createFacility(segmentEntity.getEndLocationHierarchy(), getNameBySegmentType(segmentEntity.getType()));
        return isFacilityAllowed(startFacility) || isFacilityAllowed(endFacility);
    }

    private boolean isFacilityAllowed(Facility facility) {
        return facilityLocationPermissionChecker.isFacilityLocationCovered(facility);
    }

    private PackageJourneySegment toPackageJourneySegment(Tuple tuple) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(tuple.get(BaseEntity_.ID, String.class));
        segment.setJourneyId(tuple.get(PackageJourneySegmentEntity_.SHIPMENT_JOURNEY_ID, String.class));
        segment.setRefId(tuple.get(PackageJourneySegmentEntity_.REF_ID, String.class));
        segment.setSequence(tuple.get(PackageJourneySegmentEntity_.SEQUENCE, String.class));
        segment.setLockOutTime(tuple.get(PackageJourneySegmentEntity_.LOCK_OUT_TIME, String.class));
        segment.setLockOutTimezone(tuple.get(PackageJourneySegmentEntity_.LOCK_OUT_TIMEZONE, String.class));
        segment.setStatus(SegmentStatus.valueOf(tuple.get(PackageJourneySegmentEntity_.STATUS, String.class)));
        String transportType = tuple.get(PackageJourneySegmentEntity_.TRANSPORT_TYPE, String.class);
        if (StringUtils.isNotBlank(transportType)) {
            segment.setTransportType(TransportType.valueOf(transportType));
        }
        return segment;
    }

    @Transactional
    public boolean updateSegmentByMilestone(Milestone milestone, String transactionId, boolean isNewMilestoneAfterMostRecentMilestone) {
        if (milestone == null || milestone.getMilestoneCode() == null || StringUtils.isBlank(milestone.getSegmentId())) {
            return false;
        }
        PackageJourneySegmentEntity packageJourneySegmentEntity = packageJourneySegmentRepository
                .findByIdAndOrganizationId(milestone.getSegmentId(), userDetailsProvider.getCurrentOrganizationId())
                .orElseThrow(() -> new SegmentNotFoundException(String.format(ERR_SEGMENT_NOT_FOUND, milestone.getSegmentId(), transactionId),
                        transactionId
                ));
        return updateSegmentByMilestone(packageJourneySegmentEntity, milestone, isNewMilestoneAfterMostRecentMilestone);
    }

    @Transactional
    public boolean updateSegmentByMilestone(PackageJourneySegmentEntity packageJourneySegmentEntity, Milestone milestone,
                                            boolean isNewMilestoneAfterMostRecentMilestone) {
        boolean isSegmentStatusUpdated = false;
        if (isNewMilestoneAfterMostRecentMilestone) {
            isSegmentStatusUpdated = updateSegmentStatusByMilestone(packageJourneySegmentEntity, milestone);
        }
        boolean hasDriverAndVehicleInfoUpdate = updateSegmentDriverAndVehicleFromMilestone(packageJourneySegmentEntity, milestone);
        boolean hasOnSiteTimeUpdate = updateOnSiteTime(packageJourneySegmentEntity, milestone);
        boolean hasActualTimeUpdate = updateActualTime(packageJourneySegmentEntity, milestone);

        packageJourneySegmentRepository.save(packageJourneySegmentEntity);
        return isSegmentStatusUpdated || hasDriverAndVehicleInfoUpdate || hasOnSiteTimeUpdate || hasActualTimeUpdate;
    }

    public boolean updateSegmentDriverAndVehicleFromMilestone(final PackageJourneySegmentEntity segmentEntity,
                                                              final Milestone milestone) {
        if (!MilestoneCodeUtil.isCodeUpdatingDriver(milestone.getMilestoneCode())) {
            return false;
        }

        boolean isUpdated = false;
        Driver currentDriver = segmentEntity.getDriver();
        Driver newDriver = driverMapper.milestoneToDriver(milestone);
        if ((currentDriver == null && newDriver != null)
                || (currentDriver != null && !currentDriver.equals(newDriver))) {
            segmentEntity.setDriver(newDriver);
            isUpdated = true;
        }

        Vehicle currentVehicle = segmentEntity.getVehicle();
        Vehicle newVehicle = vehicleMapper.milestoneToVehicle(milestone);
        if ((currentVehicle == null && newVehicle != null)
                || (currentVehicle != null && !currentVehicle.equals(newVehicle))) {
            segmentEntity.setVehicle(newVehicle);
            isUpdated = true;
        }
        return isUpdated;
    }

    @Transactional
    public boolean updateOnSiteTime(PackageJourneySegmentEntity packageJourneySegmentEntity, Milestone milestone) {
        if (!MilestoneCodeUtil.isCodeDriverArrived(milestone.getMilestoneCode()) || packageJourneySegmentEntity == null) {
            return false;
        }

        String milestoneTime = milestone.getMilestoneTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY.equals(milestone.getMilestoneCode())) {
            packageJourneySegmentEntity.setDropOffOnSiteTime(milestoneTimeToZoneDateTime(milestoneTime));
            packageJourneySegmentEntity.setDropOffOnSiteTimezone(getOffset(milestoneTime, packageJourneySegmentEntity.getDropOffTimezone()));
        } else {
            packageJourneySegmentEntity.setPickUpOnSiteTime(milestoneTimeToZoneDateTime(milestoneTime));
            packageJourneySegmentEntity.setPickUpOnSiteTimezone(getOffset(milestoneTime, packageJourneySegmentEntity.getPickUpTimezone()));
        }
        return true;
    }

    public boolean updateOnSiteTimeFromMilestone(final PackageJourneySegmentEntity segmentEntity,
                                                 final Milestone milestone) {
        if (!MilestoneCodeUtil.isCodeDriverArrived(milestone.getMilestoneCode())) {
            return false;
        }

        boolean isUpdated = false;
        String milestoneTime = milestone.getMilestoneTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String newTime = milestoneTimeToZoneDateTime(milestoneTime);
        if (MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY.equals(milestone.getMilestoneCode())) {
            String currentTime = segmentEntity.getDropOffOnSiteTime();
            if (!newTime.equals(currentTime)) {
                segmentEntity.setDropOffOnSiteTime(newTime);
                segmentEntity.setDropOffOnSiteTimezone(getOffset(milestoneTime, segmentEntity.getDropOffTimezone()));
                isUpdated = true;
            }
        } else {
            String currentTime = segmentEntity.getPickUpOnSiteTime();
            if (!newTime.equals(currentTime)) {
                segmentEntity.setPickUpOnSiteTime(newTime);
                segmentEntity.setPickUpOnSiteTimezone(getOffset(milestoneTime, segmentEntity.getPickUpTimezone()));
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    @Transactional
    public boolean updateActualTime(PackageJourneySegmentEntity packageJourneySegmentEntity, Milestone milestone) {
        if (!MilestoneCodeUtil.isDispatchSuccessful(milestone.getMilestoneCode())
                || StringUtils.isBlank(milestone.getSegmentId())) {
            return false;
        }

        String milestoneTime = milestone.getMilestoneTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (MilestoneCode.DSP_PICKUP_SUCCESSFUL.equals(milestone.getMilestoneCode())) {
            packageJourneySegmentEntity.setPickUpActualTime(milestoneTimeToZoneDateTime(milestoneTime));
            packageJourneySegmentEntity.setPickUpActualTimezone(getOffset(milestoneTime, packageJourneySegmentEntity.getPickUpTimezone()));
        } else {
            packageJourneySegmentEntity.setDropOffActualTime(milestoneTimeToZoneDateTime(milestoneTime));
            packageJourneySegmentEntity.setDropOffActualTimezone(getOffset(milestoneTime, packageJourneySegmentEntity.getDropOffTimezone()));
        }
        return true;
    }

    public boolean updateActualTimeFromMilestone(final PackageJourneySegmentEntity segmentEntity,
                                                 final Milestone milestone) {
        if (!MilestoneCodeUtil.isDispatchSuccessful(milestone.getMilestoneCode())) {
            return false;
        }

        boolean isUpdated = false;
        String milestoneTime = milestone.getMilestoneTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String newTime = milestoneTimeToZoneDateTime(milestoneTime);
        if (MilestoneCode.DSP_PICKUP_SUCCESSFUL.equals(milestone.getMilestoneCode())) {
            String currentTime = segmentEntity.getPickUpActualTime();
            if (!newTime.equals(currentTime)) {
                segmentEntity.setPickUpActualTime(newTime);
                segmentEntity.setPickUpActualTimezone(getOffset(milestoneTime, segmentEntity.getPickUpTimezone()));
                isUpdated = true;
            }
        } else {
            String currentTime = segmentEntity.getDropOffActualTime();
            if (!newTime.equals(currentTime)) {
                segmentEntity.setDropOffActualTime(newTime);
                segmentEntity.setDropOffActualTimezone(getOffset(milestoneTime, segmentEntity.getDropOffTimezone()));
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    @Transactional
    public void deleteAllMarkedForDeletion() {
        packageJourneySegmentRepository.deleteAllMarkedForDeletion();
    }

    public List<PackageJourneySegmentEntity> findAllByMarkedForDeletion() {
        return packageJourneySegmentRepository.findAllByMarkedForDeletion();
    }

    public void enrichSegmentsWithOrderInstructions(String orderId, List<PackageJourneySegment> segments) {
        List<InstructionEntity> orderInstructionEntities = instructionFetchService.findByOrderId(orderId);
        List<Instruction> orderInstructions = orderInstructionEntities.stream().map(INSTRUCTION_MAPPER::toDomain).toList();
        InstructionUtil.enrichOrderInstructionsToSegments(orderInstructions, segments);
    }

    public PackageJourneySegmentEntity initializeSegmentFacilitiesAndPartner(SegmentReferenceHolder segmentReferenceHolder,
                                                                             PackageJourneySegment segment,
                                                                             String orderPickupTimeZone,
                                                                             String deliveryTimeZone) {
        PackageJourneySegmentEntity segmentEntity = PackageJourneySegmentMapper.mapDomainToEntity(segment);
        return initializeLocationHierarchiesFromFacilities(segmentReferenceHolder, segmentEntity, segment, orderPickupTimeZone, deliveryTimeZone);
    }

    public PackageJourneySegmentEntity enrichSegmentWithJourneyInformation(PackageJourneySegmentEntity segmentEntity,
                                                                           ShipmentJourneyEntity journeyEntity) {
        if (segmentEntity != null && journeyEntity != null) {
            segmentEntity.setShipmentJourney(journeyEntity);
            segmentEntity.setShipmentJourneyId(journeyEntity.getId());
        }
        return segmentEntity;
    }

    public PackageJourneySegmentEntity enrichSegmentWithOrderOpsTypeInformation(PackageJourneySegmentEntity segmentEntity,
                                                                                Order order) {
        if (segmentEntity != null && order != null) {
            segmentEntity.setOpsType(order.getOpsType());
        }
        return segmentEntity;
    }

    private String milestoneTimeToZoneDateTime(String milestoneTime) {
        OffsetDateTime milestoneOffsetDateTime = OffsetDateTime.parse(milestoneTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ZoneId zoneId = ZoneId.from(milestoneOffsetDateTime);
        ZonedDateTime zonedDateTime = milestoneOffsetDateTime.atZoneSameInstant(zoneId);
        return zonedDateTime.format(ZONED_DATE_TIME_FORMAT);
    }

}
