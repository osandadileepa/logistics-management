package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.ResponseCode;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.exception.IllegalMilestoneException;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.api.exception.InvalidMilestoneException;
import com.quincus.shipment.api.exception.MilestoneNotFoundException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.impl.config.ShipmentProperties;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.MilestoneRepository;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;
import com.quincus.web.common.exception.model.MissingMandatoryFieldsException;
import com.quincus.web.common.exception.model.ObjectNotFoundException;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.exception.model.ResourceMismatchException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.Tuple;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.quincus.ext.DateTimeUtil.getOffsetFromTimezoneInformation;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_CONSOLIDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_DECONSOLIDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_DIMS_WEIGHT_UPDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_SORTED_IN_HUB;
import static com.quincus.shipment.api.domain.Shipment.DESTINATION_PROPERTY_NAME;
import static com.quincus.shipment.api.domain.Shipment.ORIGIN_PROPERTY_NAME;
import static com.quincus.shipment.impl.helper.DispatchModuleUtil.FAILED_STATUS_CODES_FROM_DISPATCH;
import static com.quincus.shipment.impl.helper.DispatchModuleUtil.RECENT_MILESTONE_EVENTS_COUNT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MilestoneService {

    private static final String INFO_RETRIEVE_RECENT_MILESTONES = "Retrieving recent milestone events of shipmentId {}";
    private static final String INFO_RECENT_STATUS_CODES = "Status codes of the recent milestone events are {}";
    private static final String INFO_CONVERTING_DISPATCH_MESSAGE_TO_MILESTONE = "Creating milestone from dispatch message. UUID: {}, DSP Payload: {}";
    private static final String ERROR_INVALID_MILESTONE_FROM_DISPATCH = "Invalid milestone message received from dispatch. UUID: {}, Milestone: {}";
    private static final String ERROR_QPORTAL_MILESTONE_ORG_NOT_FOUND = "Organization ID {} not found while calling QPortal Milestone Lookup";
    private static final String ERR_FAILED_TO_CONVERT_MESSAGE = "Failed to convert message to Milestone. Error: %s";
    private static final String ERR_UPDATE_MILESTONE_NOT_ALLOWED = "Update operation not allowed for milestone on Segment %s.";
    private static final String DEBUG_SAVING_MILESTONE_FROM_APIG = "Saving milestone message from APIG. Milestone: {}";
    private static final String FLIGHT_DEPARTED_MILESTONE_NAME = "Flight Departed";
    private static final String FLIGHT_ARRIVED_MILESTONE_NAME = "Flight Arrived";
    private static final String ERR_MSG_SHIPMENT_ID_NOT_FOUND = "with Shipment id %s";
    private static final String ERR_SEGMENT_WITH_MILESTONE_CODE_NOT_FOUND = "with Segment %s and Code %s";
    private static final String ERR_RECORD_WITH_ORDER_NUMBER_NOT_FOUND = "with Order Number ";
    private static final String MUST_NOT_BE_BLANK = "must not be blank";
    private static final String INVALID_FIELD_TYPE = "Invalid field type";
    private static final String FILE_NAME_QUERY_PARAM = "file_name";
    private static final String DIRECTORY_QUERY_PARAM = "directory";
    private static final String SHIPMENT_ATTACHMENTS_DIRECTORY = "shipment_attachments";
    private static final String SHIPMENT_TRACKING_ID = "shipmentTrackingId";
    private static final String ORGANIZATION_ID = "organizationId";
    private static final String SEGMENT_ID = "segmentId";
    private static final MilestoneMapper MILESTONE_MAPPER = Mappers.getMapper(MilestoneMapper.class);
    private final ShipmentRepository shipmentRepository;
    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;
    private final PackageJourneySegmentRepository segmentRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final QPortalService qPortalService;
    private final UserDetailsProvider userDetailsProvider;
    private final FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    private final MessageApi messageApi;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final MilestoneHubLocationHandler milestoneHubLocationHandler;
    private final ShipmentProperties shipmentProperties;
    private final MilestoneTimezoneHelper milestoneTimezoneHelper;

    private List<Tuple> findRecentMilestoneEventsByShipmentId(String shipmentId) {
        log.info(INFO_RETRIEVE_RECENT_MILESTONES, shipmentId);

        return milestoneRepository.findByShipmentId(
                shipmentId,
                PageRequest.of(
                        0,
                        RECENT_MILESTONE_EVENTS_COUNT,
                        Sort.by(BaseEntity_.MODIFY_TIME).descending()
                )
        ).getContent();
    }

    public boolean isRetryableToDispatch(String shipmentId) {
        List<Tuple> recentMilestoneEvents = findRecentMilestoneEventsByShipmentId(shipmentId);

        List<MilestoneCode> codes = recentMilestoneEvents.stream()
                .map(MilestoneMapper::mapTupleToEntity)
                .map(MilestoneEntity::getMilestoneCode)
                .toList();

        log.debug(INFO_RECENT_STATUS_CODES, codes);

        for (MilestoneCode failedStatusCode : FAILED_STATUS_CODES_FROM_DISPATCH) {
            if (Collections.frequency(codes, failedStatusCode) == RECENT_MILESTONE_EVENTS_COUNT) {
                return false;
            }
        }
        return true;
    }

    public boolean isFailedStatusCode(MilestoneCode code) {
        return code != null && FAILED_STATUS_CODES_FROM_DISPATCH.contains(code);
    }

    @Transactional
    public MilestoneEntity save(MilestoneEntity milestoneEntity) {
        return milestoneRepository.save(milestoneEntity);
    }

    @Transactional
    public Milestone createMilestone(Milestone milestone) {
        MilestoneEntity milestoneEntity = save(milestoneMapper.toEntity(milestone));
        milestone.setId(milestoneEntity.getId());
        return milestone;
    }

    public Milestone createPackageDimensionUpdateMilestone(Shipment shipment) {
        Milestone milestone = createCommonMilestone(shipment);
        milestone.setMilestoneCode(MilestoneCode.SHP_DIMS_WEIGHT_UPDATED);
        enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestones());
        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);
        milestone.setMilestoneTime(generateMilestoneTimeWithHubTimezoneOffset(milestone.getHubTimeZone()));
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        return createMilestone(milestone);
    }

    private Milestone createCommonMilestone(Shipment shipment) {
        PackageJourneySegment segment = shipment.getShipmentJourney()
                .getPackageJourneySegments()
                .stream()
                .filter(e -> e.getStatus() != SegmentStatus.COMPLETED).findFirst().orElse(null);
        return createCommonMilestone(shipment, segment);
    }

    private Milestone createCommonMilestone(Shipment shipment, PackageJourneySegment segment) {
        Milestone milestone = new Milestone();
        milestone.setShipmentId(shipment.getId());
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        milestone.setUserId(shipment.getUserId());
        milestone.setPartnerId(shipment.getPartnerId());
        if (nonNull(segment)) {
            milestone.setFromLocationId(segment.getStartFacility().getExternalId());
            if (nonNull(segment.getStartFacility().getLocation())) {
                milestone.setFromCountryId(segment.getStartFacility().getLocation().getCountryId());
                milestone.setFromStateId(segment.getStartFacility().getLocation().getStateId());
                milestone.setFromCityId(segment.getStartFacility().getLocation().getCityId());
            }
            milestone.setToLocationId(segment.getEndFacility().getExternalId());
            if (nonNull(segment.getEndFacility().getLocation())) {
                milestone.setToCountryId(segment.getEndFacility().getLocation().getCountryId());
                milestone.setToStateId(segment.getEndFacility().getLocation().getStateId());
                milestone.setToCityId(segment.getEndFacility().getLocation().getCityId());
            }
        }
        return milestone;
    }

    public Milestone createOMTriggeredMilestoneAndSendMilestoneMessage(Shipment shipment, MilestoneCode milestoneCode) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);
        milestone.setShipmentId(shipment.getId());
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        milestone.setUserName(userDetailsProvider.getCurrentUserFullName());
        milestone.setUserId(shipment.getUserId());
        milestone.setPartnerId(shipment.getPartnerId());
        milestone.setFromLocationId(shipment.getPickUpLocation());
        milestone.setToLocationId(shipment.getDeliveryLocation());
        enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestones());
        Address origin = shipment.getOrigin();
        if (origin != null) {
            milestone.setFromCountryId(origin.getCountryId());
            milestone.setFromStateId(origin.getStateId());
            milestone.setFromCityId(origin.getCityId());
        }
        Address destination = shipment.getDestination();
        if (destination != null) {
            milestone.setToCountryId(destination.getCountryId());
            milestone.setToStateId(destination.getStateId());
            milestone.setToCityId(destination.getCityId());
        }
        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);
        String orderCreationTime = Optional.ofNullable(shipment.getOrder().getTimeCreated()).orElse(ZonedDateTime.now(Clock.systemUTC()).toString());
        ZoneId hubZoneId = getOffsetFromTimezoneInformation(milestone.getHubTimeZone());
        OffsetDateTime milestoneTime = DateTimeUtil.toFormattedOffsetDateTime(orderCreationTime);
        if (milestoneTime != null && hubZoneId != null) {
            milestoneTime = milestoneTime.atZoneSameInstant(hubZoneId).toOffsetDateTime();
        }
        milestone.setMilestoneTime(milestoneTime);
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        Milestone createdMilestone = createMilestone(milestone);
        shipment.setMilestone(createdMilestone);
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.OM);
        return createdMilestone;
    }

    @Transactional
    public Milestone createMilestoneFromOpsUpdate(ShipmentEntity shipmentEntity,
                                                  PackageJourneySegmentEntity currentActiveSegmentEntity,
                                                  ShipmentMilestoneOpsUpdateRequest milestoneOpsUpdateRequest) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.parse(milestoneOpsUpdateRequest.getMilestoneTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        milestone.setShipmentId(shipmentEntity.getId());
        milestone.setOrganizationId(shipmentEntity.getOrganization().getId());
        milestone.setHubId(milestoneOpsUpdateRequest.getUsersLocation().getLocationId());
        milestone.setUserName(userDetailsProvider.getCurrentUserFullName());
        milestone.setUserId(userDetailsProvider.getCurrentUserId());
        milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone);
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(milestone, currentActiveSegmentEntity);
        milestone.setPartnerId(shipmentEntity.getPartnerId());
        milestone.setMilestoneCode(getMilestoneCodeOrNull(milestoneOpsUpdateRequest.getMilestoneCode()));
        milestone.setMilestoneName(milestoneOpsUpdateRequest.getMilestoneName());
        milestone.setAdditionalInfo(createAdditionalInfoFromOpsUpdate(milestoneOpsUpdateRequest, milestone.getMilestoneTime()));
        enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestones());
        if (nonNull(currentActiveSegmentEntity)) {
            
            milestone.setSegmentId(currentActiveSegmentEntity.getId());
            milestone.setPartnerId(Optional.ofNullable(currentActiveSegmentEntity.getPartner()).map(PartnerEntity::getExternalId).orElse(null));

            Optional<Driver> optionalDriver = Optional.ofNullable(currentActiveSegmentEntity.getDriver());

            milestone.setDriverPhoneCode(optionalDriver.map(Driver::getPhoneCode).orElse(null));
            milestone.setDriverPhoneNumber(optionalDriver.map(Driver::getPhoneNumber).orElse(null));
            milestone.setDriverId(optionalDriver.map(Driver::getId).orElse(null));
        }
        MilestoneEntity newMilestone = milestoneRepository.saveAndFlush(milestoneMapper.toEntity(milestone));
        milestone.setId(newMilestone.getId());
        Set<MilestoneEntity> existingMilestoneEvents = shipmentEntity.getMilestoneEvents();
        if (CollectionUtils.isEmpty(existingMilestoneEvents)) {
            shipmentEntity.setMilestoneEvents(Set.of(newMilestone));
        } else {
            Set<MilestoneEntity> newSetOfMilestoneEvents = new HashSet<>(existingMilestoneEvents);
            newSetOfMilestoneEvents.add(newMilestone);
            shipmentEntity.setMilestoneEvents(newSetOfMilestoneEvents);
        }
        return milestone;
    }

    public Milestone initializeDispatchMilestone(String dspPayload, String messageTransactionId) {
        log.debug(INFO_CONVERTING_DISPATCH_MESSAGE_TO_MILESTONE, messageTransactionId, dspPayload);
        try {
            Milestone dspMilestone = milestoneMapper.convertMessageToDomain(dspPayload);
            dspMilestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
            MilestoneLookup refMilestone = lookupMilestoneCode(dspMilestone.getMilestoneCode(), qPortalService.listMilestones());
            if (refMilestone != null) {
                dspMilestone.setMilestoneRefId(refMilestone.getId());
                if (StringUtils.isEmpty(dspMilestone.getMilestoneName())) {
                    dspMilestone.setMilestoneName(refMilestone.getName());
                }
            }
            return dspMilestone;
        } catch (JsonProcessingException e) {
            throw new InvalidMilestoneException(String.format(ERR_FAILED_TO_CONVERT_MESSAGE, dspPayload), messageTransactionId);
        }
    }

    public JsonNode getDispatchMessageJson(String dspPayload) {
        try {
            return objectMapper.readTree(dspPayload);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return objectMapper.createObjectNode();
    }

    public void validateDispatchMilestone(Milestone dspMilestone, String uuid) {
        try {
            validateMilestone(dspMilestone);
        } catch (ConstraintViolationException e) {
            log.warn(ERROR_INVALID_MILESTONE_FROM_DISPATCH, uuid, dspMilestone.getMilestoneCode(), e);
            throw e;
        }

        Optional<PackageJourneySegmentEntity> optSegment = segmentRepository.findByIdAndOrganizationIdAndShipmentId(dspMilestone.getSegmentId(),
                userDetailsProvider.getCurrentOrganizationId(), dspMilestone.getShipmentId());

        if (optSegment.isEmpty()) {
            log.error(ERROR_INVALID_MILESTONE_FROM_DISPATCH, uuid, dspMilestone);
            throw new IllegalMilestoneException(
                    Map.of(
                            SEGMENT_ID, dspMilestone.getSegmentId(),
                            ORGANIZATION_ID, dspMilestone.getOrganizationId(),
                            SHIPMENT_TRACKING_ID, dspMilestone.getShipmentTrackingId()
                    ),
                    uuid
            );
        }
    }

    public Milestone getLatestMilestone(Set<MilestoneEntity> milestoneEntities) {
        if (CollectionUtils.isEmpty(milestoneEntities)) {
            return null;
        }
        Optional<MilestoneEntity> latestOptionalMilestone = milestoneEntities.stream().findFirst();
        return latestOptionalMilestone.map(milestoneMapper::toDomain).orElse(null);
    }

    public boolean isMilestoneHubActivity(MilestoneCode code) {
        return SHP_CONSOLIDATED.equals(code)
                || SHP_DECONSOLIDATED.equals(code)
                || SHP_DIMS_WEIGHT_UPDATED.equals(code)
                || SHP_SORTED_IN_HUB.equals(code);
    }

    public void setMilestoneEventsForShipment(ShipmentEntity shipmentEntity, Shipment shipment) {
        if (shipmentEntity.getMilestoneEvents() == null) return;
        shipment.setMilestoneEvents(
                shipmentEntity.getMilestoneEvents().stream()
                        .map(milestoneMapper::toDomain)
                        .sorted((m1, m2) -> m2.getMilestoneTime().compareTo(m1.getMilestoneTime()))
                        .toList());
        setMostRecentMilestone(shipment);
        classifyMilestoneEvents(shipment.getMilestoneEvents(), shipment);
    }

    public void setMostRecentMilestone(Shipment shipment) {
        if (isNull(shipment.getMilestoneEvents())) return;
        shipment.getMilestoneEvents().stream().max(Comparator.comparing(Milestone::getMilestoneTime)).ifPresent(shipment::setMilestone);
    }

    private MilestoneCode getMilestoneCodeOrNull(String milestoneCode) {
        try {
            return MilestoneCode.fromValue(milestoneCode);
        } catch (InvalidEnumValueException ignored) {
            return null;
        }
    }

    private MilestoneAdditionalInfo createAdditionalInfoFromOpsUpdate(ShipmentMilestoneOpsUpdateRequest milestoneOpsUpdateRequest,
                                                                      OffsetDateTime milestoneTime) {
        MilestoneAdditionalInfo additionalInfo = new MilestoneAdditionalInfo();
        if (CollectionUtils.isEmpty(milestoneOpsUpdateRequest.getAttachments())) {
            if (milestoneOpsUpdateRequest.getNotes() == null) {
                return null;
            }
            additionalInfo.setAttachments(Collections.emptyList());
        } else {
            milestoneOpsUpdateRequest.getAttachments().forEach(attachment -> {
                setFileURLWithPreSignedURL(attachment);
                attachment.setFileTimestamp(milestoneTime);
            });
            additionalInfo.setAttachments(milestoneOpsUpdateRequest.getAttachments());
        }
        additionalInfo.setImages(Collections.emptyList());
        additionalInfo.setSignature(Collections.emptyList());
        additionalInfo.setRemarks(milestoneOpsUpdateRequest.getNotes());
        return additionalInfo;
    }

    private void setFileURLWithPreSignedURL(final HostedFile file) {
        file.setDirectFileUrl(file.getFileUrl());
        final String readPreSignedUrl = shipmentProperties.getBaseUrl() + shipmentProperties.getReadPreSignedPath();
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(readPreSignedUrl);
        builder.queryParam(FILE_NAME_QUERY_PARAM, file.getFileName());
        builder.queryParam(DIRECTORY_QUERY_PARAM, SHIPMENT_ATTACHMENTS_DIRECTORY);
        file.setFileUrl(builder.toUriString());
    }

    private void classifyMilestoneEvents(List<Milestone> milestones, Shipment shipment) {
        ShipmentJourney journey = shipment.getShipmentJourney();
        if (isNull(journey)) {
            return;
        }

        for (PackageJourneySegment segment : journey.getPackageJourneySegments()) {
            Partner partner = segment.getPartner();
            Facility startFacility = segment.getStartFacility();
            Facility endFacility = segment.getEndFacility();

            if (!isNull(partner) && !isNull(partner.getId())) {
                List<Milestone> vendorEvents = milestones.stream()
                        .filter(m -> segment.getSegmentId().equals(m.getSegmentId()))
                        .filter(m -> (!isNull(m.getPartnerId()) && (m.getPartnerId().equalsIgnoreCase(partner.getId()))))
                        .toList();
                partner.setVendorEvents(vendorEvents);
            }

            if (!isNull(startFacility) && !isNull(startFacility.getExternalId())
                    && !ORIGIN_PROPERTY_NAME.equals(startFacility.getCode())) {
                List<Milestone> hubEvents = milestones.stream()
                        .filter(m -> segment.getSegmentId().equals(m.getSegmentId()))
                        .filter(m -> isMilestoneHubActivity(m.getMilestoneCode()))
                        .filter(m -> (!isNull(m.getHubId()) && (m.getHubId().equalsIgnoreCase(startFacility.getExternalId()))))
                        .toList();
                startFacility.setHubEvents(hubEvents);
            }

            if (!isNull(endFacility) && !isNull(endFacility.getExternalId())
                    && !DESTINATION_PROPERTY_NAME.equals(endFacility.getCode())) {
                List<Milestone> hubEvents = milestones.stream()
                        .filter(m -> segment.getSegmentId().equals(m.getSegmentId()))
                        .filter(m -> isMilestoneHubActivity(m.getMilestoneCode()))
                        .filter(m -> (!isNull(m.getHubId()) && (m.getHubId().equalsIgnoreCase(endFacility.getExternalId()))))
                        .toList();
                endFacility.setHubEvents(hubEvents);
            }
        }
    }

    public Milestone convertAndValidateMilestoneFromDispatch(String dspPayload, String uuid) {
        Milestone dspMilestone = initializeDispatchMilestone(dspPayload, uuid);
        validateDispatchMilestone(dspMilestone, uuid);
        return dspMilestone;
    }

    public OffsetDateTime getMostRecentMilestoneTimeByShipmentId(String shipmentId) {
        List<Tuple> recentMilestoneEvents = findRecentMilestoneEventsByShipmentId(shipmentId);
        if (CollectionUtils.isNotEmpty(recentMilestoneEvents)) {
            return OffsetDateTime.parse(MilestoneMapper.mapTupleToEntity(recentMilestoneEvents.get(0)).getMilestoneTime());
        }
        return null;
    }

    private void validateMilestone(Milestone milestone) {
        Set<ConstraintViolation<Milestone>> violations = validator.validate(milestone);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<Milestone> constraintViolation : violations) {
                errorMessage.append(constraintViolation.getPropertyPath())
                        .append(" ")
                        .append(constraintViolation.getMessage())
                        .append("\n");
            }
            throw new ConstraintViolationException("Milestone error violations occurred: " + errorMessage, violations);
        }
    }

    @Transactional
    public Milestone createMilestoneFromFlightEvent(ShipmentMessageDto shipmentMessageDto, PackageJourneySegment segment, Flight flight) {
        String organizationId = shipmentMessageDto.getOrganizationId();
        FlightStatus flightStatus = flight.getFlightStatus();
        Milestone milestone = new Milestone();
        if (flightStatus.getEventName() == FlightEventName.FLIGHT_DEPARTED) {
            updateMilestoneHubDetailsFromFacility(milestone, segment.getStartFacility());
            milestone.setMilestoneName(FLIGHT_DEPARTED_MILESTONE_NAME);
            milestone.setMilestoneCode(MilestoneCode.SHP_FLIGHT_DEPARTED);
            enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestonesByOrganizationId(organizationId));
        } else if (flightStatus.getEventName() == FlightEventName.FLIGHT_LANDED) {
            updateMilestoneHubDetailsFromFacility(milestone, segment.getEndFacility());
            milestone.setMilestoneName(FLIGHT_ARRIVED_MILESTONE_NAME);
            milestone.setMilestoneCode(MilestoneCode.SHP_FLIGHT_ARRIVED);
            enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestonesByOrganizationId(organizationId));
        }
        milestone.setPartnerId(Optional.ofNullable(segment.getPartner()).map(Partner::getId).orElse(null));
        milestone.setShipmentId(shipmentMessageDto.getId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setOrganizationId(organizationId);
        milestone.setMilestoneTime(generateMilestoneTimeWithHubTimezoneOffset(milestone.getHubTimeZone()));
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(milestone, segment);
        return createMilestone(milestone);
    }

    private OffsetDateTime generateMilestoneTimeWithHubTimezoneOffset(String hubTimezone) {
        if (StringUtils.isBlank(hubTimezone)) {
            log.warn("No hub timezone. Will use default UTC timezone");
            return OffsetDateTime.now(Clock.systemUTC());
        }
        try {
            ZoneId zoneId = ZoneId.of(hubTimezone.replaceAll("\\s.*", ""));
            return OffsetDateTime.now(zoneId);
        } catch (Exception e) {
            log.warn("Error parsing hub timezone:{} ", hubTimezone);
            return OffsetDateTime.now(Clock.systemUTC());
        }
    }

    private void updateMilestoneHubDetailsFromFacility(Milestone milestone, Facility segmentFacility) {
        Optional.ofNullable(segmentFacility).ifPresent(facility -> {
            milestone.setHubId(facility.getExternalId());
            Optional.ofNullable(facility.getLocation()).ifPresent(location -> {
                milestone.setHubCountryId(location.getCountryId());
                milestone.setHubStateId(location.getStateId());
                milestone.setHubCityId(location.getCityId());
                milestone.setHubTimeZone(facility.getTimezone());
            });
        });
    }

    public List<MilestoneEntity> findRecentMilestoneByShipmentIds(List<String> ids) {
        List<Object[]> objects = milestoneRepository.findRecentMilestoneByShipmentIds(ids);
        if (CollectionUtils.isEmpty(objects)) return Collections.emptyList();
        return convertArrayObjectToSet(objects);
    }

    private List<MilestoneEntity> convertArrayObjectToSet(List<Object[]> objects) {
        List<MilestoneEntity> entities = new ArrayList<>();
        objects.forEach(e -> {
            if (nonNull(e)) {
                entities.add(convertObjectToMilestone(e));
            }
        });
        return entities;
    }

    private MilestoneEntity convertObjectToMilestone(Object[] obj) {
        if (ArrayUtils.isEmpty(obj)) return null;
        MilestoneEntity entity = new MilestoneEntity();
        entity.setId((String) obj[0]);
        entity.setShipmentId((String) obj[1]);
        entity.setMilestoneName((String) obj[2]);
        return entity;
    }

    @Transactional
    public Milestone partialUpdate(Milestone milestone) {
        Optional<ShipmentEntity> shipmentEntity = shipmentRepository.findById(milestone.getShipmentId(), userDetailsProvider.getCurrentOrganizationId());
        if (shipmentEntity.isEmpty()) {
            String shipmentErr = String.format(ERR_MSG_SHIPMENT_ID_NOT_FOUND, milestone.getShipmentId());
            throw new ObjectNotFoundException(Shipment.class.getName(), shipmentErr);
        }
        PackageJourneySegmentEntity packageJourneySegmentEntity = findSegmentByRefId(shipmentEntity.get(), milestone.getSegmentId());
        String errorMsg = String.format(ERR_SEGMENT_WITH_MILESTONE_CODE_NOT_FOUND, milestone.getSegmentId(), milestone.getMilestoneCode());
        if (isNull(packageJourneySegmentEntity)) {
            throw new ObjectNotFoundException(Milestone.class.getName(), errorMsg);
        }
        Optional<MilestoneEntity> optMilestoneEntity = milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegment(milestone.getMilestoneCode(), milestone.getShipmentId(), packageJourneySegmentEntity);
        if (optMilestoneEntity.isEmpty()) {
            throw new ObjectNotFoundException(Milestone.class.getName(), errorMsg);
        }
        validatePermission(packageJourneySegmentEntity);
        validateOrderId(milestone.getExternalOrderId(), shipmentEntity.get());
        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(milestone, packageJourneySegmentEntity);
        MilestoneEntity milestoneEntity = milestoneMapper.toEntity(optMilestoneEntity.get(), milestone);
        Milestone savedMilestone = milestoneMapper.toDomain(milestoneRepository.save(milestoneEntity));

        if (!isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(savedMilestone)) {
            return savedMilestone;
        }

        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(savedMilestone);
        boolean hasUpdate = updateSegment(packageJourneySegmentEntity, milestone);
        Shipment shipment = ShipmentMapper.mapEntityToDomain(shipmentEntity.get(), objectMapper);
        messageApi.sendMilestoneMessage(savedMilestone, shipment);
        if (hasUpdate) {
            messageApi.sendUpdatedSegmentFromShipment(shipment, milestone.getSegmentId());
        }
        return savedMilestone;
    }

    private void validatePermission(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (!isMilestoneSegmentLocationAllowed(packageJourneySegmentEntity)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_MILESTONE_NOT_ALLOWED,
                    packageJourneySegmentEntity.getId()),
                    ShipmentErrorCode.MILESTONE_INFO_UPDATE_NOT_ALLOWED);
        }
    }

    private void validateOrderId(String externalOrderId, ShipmentEntity refShipmentEntity) {
        if (StringUtils.isBlank(externalOrderId)) {
            throw new QuincusValidationException("`external_order_id` must not be blank.");
        }

        String refId = Optional.ofNullable(refShipmentEntity.getExternalOrderId())
                .orElse(Optional.ofNullable(refShipmentEntity.getOrder())
                        .map(OrderEntity::getOrderIdLabel).orElse(""));

        if (!externalOrderId.equalsIgnoreCase(refId)) {
            throw new ResourceMismatchException("milestone", "external_order_id", externalOrderId, "shipment", "external_order_id/order_id_label");
        }
    }

    public MilestoneEntity findMilestoneFromShipmentAndSegment(MilestoneCode code, Shipment shipment,
                                                               PackageJourneySegment segment) {
        return milestoneRepository.findByMilestoneCodeAndShipmentIdAndSegmentId(code, shipment.getId(),
                segment.getSegmentId()).orElse(null);
    }

    @Transactional
    public MilestoneUpdateResponse saveMilestoneFromAPIG(MilestoneUpdateRequest milestoneRequest) {
        try {
            validate(milestoneRequest);
            Milestone milestone = milestoneMapper.toMilestone(milestoneRequest);
            milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
            enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestones());
            log.debug(DEBUG_SAVING_MILESTONE_FROM_APIG, milestone);

            List<ShipmentEntity> shipmentEntityList = shipmentRepository.findByOrderNumberAndOrganizationId(milestone.getOrderNumber(), milestone.getOrganizationId());
            if (CollectionUtils.isEmpty(shipmentEntityList)) {
                throw new ObjectNotFoundException(Milestone.class.getName(), ERR_RECORD_WITH_ORDER_NUMBER_NOT_FOUND + milestone.getOrderNumber());
            }

            Shipment referenceShipment = null;
            for (ShipmentEntity shipmentEntity : shipmentEntityList) {
                milestone.setShipmentId(shipmentEntity.getId());
                String refId = milestone.getExternalSegmentId();
                PackageJourneySegmentEntity packageJourneySegmentEntity = findSegmentByRefId(shipmentEntity, refId);
                if (isNull(packageJourneySegmentEntity)) {
                    String errorMsg = String.format(ERR_SEGMENT_WITH_MILESTONE_CODE_NOT_FOUND, refId, milestone.getMilestoneCode());
                    throw new ObjectNotFoundException(Milestone.class.getName(), errorMsg);
                }
                milestone.setSegmentId(packageJourneySegmentEntity.getId());
                Shipment shipment = ShipmentMapper.mapEntityToDomain(shipmentEntity, objectMapper);
                shipment.setMilestone(milestone);

                OffsetDateTime mostRecentMilestone = getMostRecentMilestoneTimeByShipmentId(milestone.getShipmentId());
                milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);
                milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
                milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(milestone, packageJourneySegmentEntity);
                createMilestone(milestone);

                if (nonNull(mostRecentMilestone) && isNewMilestoneAfterMostRecentMilestone(milestone.getMilestoneTime(), mostRecentMilestone)) {
                    referenceShipment = shipment;
                }

                sendMilestoneMessage(milestone, referenceShipment);
            }
            return createMilestoneUpdateResponse(milestoneRequest, ResponseCode.SCC0000, "");
        } catch (MissingMandatoryFieldsException e) {
            MilestoneUpdateResponse response = createMilestoneUpdateResponse(milestoneRequest, ResponseCode.ERR0003, e.getMessage());
            throw new MissingMandatoryFieldsException(response, e.getMessage(), e);
        } catch (InvalidFieldTypeException e) {
            MilestoneUpdateResponse response = createMilestoneUpdateResponse(milestoneRequest, ResponseCode.ERR0002, e.getMessage());
            throw new InvalidFieldTypeException(response, e.getMessage(), e);
        } catch (ObjectNotFoundException e) {
            MilestoneUpdateResponse response = createMilestoneUpdateResponse(milestoneRequest, ResponseCode.ERR0005, e.getName());
            throw new ObjectNotFoundException(response, e.getMessage(), e);
        } catch (Exception e) {
            MilestoneUpdateResponse response = createMilestoneUpdateResponse(milestoneRequest, ResponseCode.ERR9999, e.getMessage());
            throw new QuincusException(response, e.getMessage(), e);
        }
    }

    private void sendMilestoneMessage(Milestone milestone, Shipment referenceShipment) {
        if (referenceShipment != null) {
            boolean hasUpdate = packageJourneySegmentService.updateSegmentStatusByMilestoneEvent(milestone, null);
            if (hasUpdate) {
                packageJourneySegmentService.refreshJourneyWithUpdatedSegments(referenceShipment.getShipmentJourney());
                messageApi.sendUpdatedSegmentFromShipment(referenceShipment, milestone.getSegmentId());
            }
            messageApi.sendMilestoneMessage(referenceShipment, TriggeredFrom.APIG);
        }
    }

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        if (constraintViolations.isEmpty()) return;
        String missingFields = getFieldError(constraintViolations, MUST_NOT_BE_BLANK);
        if (StringUtils.isNotBlank(missingFields)) {
            throw new MissingMandatoryFieldsException(missingFields);
        }

        String invalidFieldType = getFieldError(constraintViolations, INVALID_FIELD_TYPE);
        if (StringUtils.isNotBlank(invalidFieldType)) {
            throw new InvalidFieldTypeException(invalidFieldType);
        }

        throw new ConstraintViolationException("Milestone error violations occurred: ", constraintViolations);
    }

    public boolean isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(MilestoneCode milestoneCode, Shipment refShipment,
                                                                                          String segmentId) {
        int result = milestoneRepository.isAllShipmentFromOrderSameMilestone(refShipment.getOrder().getId(),
                segmentId, milestoneCode.name());
        return result > 0 || isMilestoneCodePickupSuccessful(milestoneCode);
    }

    public boolean isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(Milestone milestone) {
        String milestoneCodeStr = milestone.getMilestoneCode().name();
        String shipmentId = milestone.getShipmentId();
        String segmentId = milestone.getSegmentId();

        int result = milestoneRepository.isAllRelatedShipmentSameMilestone(shipmentId,
                segmentId, milestoneCodeStr);
        return result > 0 || isMilestoneCodePickupSuccessful(milestone.getMilestoneCode());
    }

    public boolean isAllShipmentFromOrderHaveSameMilestone(Milestone milestone, Shipment refShipment) {
        String milestoneCodeStr = milestone.getMilestoneCode().name();
        String shipmentId = refShipment.getId();
        String segmentId = milestone.getSegmentId();

        int result = milestoneRepository.isAllRelatedShipmentSameMilestone(shipmentId, segmentId, milestoneCodeStr);
        return result > 0;
    }

    private boolean isMilestoneCodePickupSuccessful(MilestoneCode milestoneCode) {
        return MilestoneCode.DSP_PICKUP_SUCCESSFUL == milestoneCode;
    }

    private String getFieldError(Set<ConstraintViolation<Object>> constraintViolations, String violationType) {
        return constraintViolations.stream()
                .filter(violation -> violation.getMessage().equalsIgnoreCase(violationType))
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.joining(", "));
    }

    private PackageJourneySegmentEntity findSegmentByRefId(ShipmentEntity shipmentEntity, String refId) {
        return shipmentEntity
                .getShipmentJourney()
                .getPackageJourneySegments()
                .stream()
                .filter(pjs -> StringUtils.equalsIgnoreCase(pjs.getRefId(), refId))
                .findAny().orElse(null);
    }

    private boolean updateSegment(PackageJourneySegmentEntity packageJourneySegmentEntity, Milestone milestone) {
        boolean isHubIdChanged = !StringUtils.equalsIgnoreCase(milestone.getHubId(), packageJourneySegmentEntity.getHubId());
        if (isHubIdChanged) {
            packageJourneySegmentEntity.setHubId(milestone.getHubId());
            segmentRepository.save(packageJourneySegmentEntity);
            return true;
        }
        return false;
    }

    private boolean isMilestoneSegmentLocationAllowed(@NotNull PackageJourneySegmentEntity segmentEntity) {
        PackageJourneySegment segment = PackageJourneySegmentMapper.mapEntityToDomain(false, segmentEntity, 0, 0);
        return facilityLocationPermissionChecker.isFacilityLocationCovered(segment.getStartFacility())
                || facilityLocationPermissionChecker.isFacilityLocationCovered(segment.getEndFacility());
    }

    public MilestoneUpdateResponse createMilestoneUpdateResponse(MilestoneUpdateRequest milestone, ResponseCode responseCode, String message) {
        MilestoneUpdateResponse response = new MilestoneUpdateResponse();
        response.setOrderNumber(milestone.getOrderNumber());
        response.setSegmentId(milestone.getSegmentId());
        response.setMilestone(milestone.getMilestone());
        response.setVendorId(milestone.getVendorId());
        response.setShipmentIds(milestone.getShipmentIds());
        response.setResponseCode(responseCode);
        response.setResponseMessage(String.format(responseCode.getMessage(), message));
        response.setTimestamp(Instant.now(Clock.systemUTC()));
        return response;
    }

    public void enrichMilestoneWithQPortalInfo(Milestone milestone, List<MilestoneLookup> qPortalRefMilestoneList) {
        MilestoneLookup refMilestone = lookupMilestoneCode(milestone.getMilestoneCode(), qPortalRefMilestoneList);
        if (refMilestone != null) {
            milestone.setMilestoneRefId(refMilestone.getId());
            if (milestone.getMilestoneName() == null) {
                milestone.setMilestoneName(refMilestone.getName());
            }
        }
    }

    private MilestoneLookup lookupMilestoneCode(MilestoneCode milestoneCode, List<MilestoneLookup> qPortalRefMilestoneList) {
        if (milestoneCode == null) {
            log.warn("The milestone code for the organization ID `{}` you provided is null, Aborting lookup process from QPortal.", userDetailsProvider.getCurrentOrganizationId());
            return null;
        }
        for (MilestoneLookup qPortalMilestone : qPortalRefMilestoneList) {
            if (qPortalMilestone.getCode().equals(milestoneCode.toString())) {
                return qPortalMilestone;
            }
        }
        log.error(ERROR_QPORTAL_MILESTONE_ORG_NOT_FOUND, userDetailsProvider.getCurrentOrganizationId());
        return null;
    }

    @Transactional
    public Milestone updateMilestoneTime(MilestoneUpdateTimeRequest milestoneUpdateTimeRequest) {
        if (!DateTimeUtil.isTodayOrPastDate(milestoneUpdateTimeRequest.getMilestoneTime())) {
            throw new QuincusValidationException(String.format("Milestone Time must be today or a past date `%s`", milestoneUpdateTimeRequest.getMilestoneTime()));
        }
        MilestoneEntity milestoneEntity = milestoneRepository.findById(milestoneUpdateTimeRequest.getId())
                .orElseThrow(() -> new MilestoneNotFoundException(String.format("Unable to find milestone with id `%s`", milestoneUpdateTimeRequest.getId())));
        if (!facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(milestoneEntity.getShipmentId())) {
            throw new SegmentLocationNotAllowedException(String.format("Updating the milestone on shipment %s is not permitted because one of the segments does not have location coverage access",
                    milestoneEntity.getShipmentId()),
                    ShipmentErrorCode.MILESTONE_INFO_UPDATE_NOT_ALLOWED);
        }
        milestoneEntity.setMilestoneTime(DateTimeUtil.toIsoDateTimeFormat(milestoneUpdateTimeRequest.getMilestoneTime()));
        return milestoneMapper.toDomain(milestoneRepository.save(milestoneEntity));
    }

    public boolean isNewMilestoneAfterMostRecentMilestone(OffsetDateTime newMileStoneTime, OffsetDateTime mostRecentMilestoneTime) {
        return newMileStoneTime.isAfter(mostRecentMilestoneTime);
    }

    @Transactional
    public void updateMilestoneAndPackageJourneySegment(@NotNull Milestone milestone, @NotNull String transactionId,
                                                        @NotNull OffsetDateTime previousMilestoneTime) {
        boolean isNewMilestoneAfterMostRecentMilestone = isNewMilestoneAfterMostRecentMilestone(milestone.getMilestoneTime(), previousMilestoneTime);
        final boolean segmentHasUpdate = packageJourneySegmentService.updateSegmentByMilestone(milestone, transactionId, isNewMilestoneAfterMostRecentMilestone);
        milestone.setSegmentUpdatedFromMilestone(segmentHasUpdate);
    }

    /**
     * This method returns the latest milestone that has a pending update on the Segment.
     * Example:
     * Newly created Order ABC contains Shipments A and B
     * Shipment A receives a milestone update (Pickup Successful)
     * - Segment will not be updated yet in this instance
     * Shipment B is cancelled/removed. This will result in Shipment B not receiving milestone for Pickup Successful.
     * The active segment for this order becomes pending in this situation.
     */
    public Optional<Milestone> getMilestoneWithPendingSegmentUpdate(List<String> missingShipmentIds) {
        List<MilestoneEntity> milestoneEntities = milestoneRepository.getMilestonesWithLikelyPendingSegmentUpdate(missingShipmentIds);
        if (CollectionUtils.isEmpty(milestoneEntities)) {
            return Optional.empty();
        }
        Collections.reverse(milestoneEntities);
        return Optional.of(milestoneEntities.get(0)).map(MILESTONE_MAPPER::toDomain);
    }

    public Milestone createVendorUpdateMilestone(Shipment shipment, PackageJourneySegment segment, MilestoneCode milestoneCode) {
        Milestone milestone = createCommonMilestone(shipment, segment);
        milestone.setMilestoneCode(milestoneCode);
        enrichMilestoneWithQPortalInfo(milestone, qPortalService.listMilestones());
        // only send hubId for mid_mile as start facility in first segment and only segment can be non facility
        if (segment.getType() == SegmentType.MIDDLE_MILE) {
            milestone.setHubId(segment.getStartFacility().getExternalId());
        }
        milestone.setHubCityId(segment.getStartFacility().getLocation().getCityId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setPartnerId(Optional.ofNullable(segment.getPartner()).map(Partner::getId).orElse(null));
        milestoneHubLocationHandler.enrichMilestoneHubLocationDetailsByHubCityId(milestone);
        milestone.setMilestoneTime(generateMilestoneTimeWithHubTimezoneOffset(milestone.getHubTimeZone()));
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        return createMilestone(milestone);
    }

    public Milestone createMilestoneFromAPIG(final MilestoneUpdateRequest milestoneUpdateRequest) {
        Milestone milestone = milestoneMapper.toMilestone(milestoneUpdateRequest);
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        MilestoneLookup refMilestone = lookupMilestoneCode(milestone.getMilestoneCode(), qPortalService.listMilestones());

        if (refMilestone != null) {
            milestone.setMilestoneRefId(refMilestone.getId());
            if (StringUtils.isEmpty(milestone.getMilestoneName())) {
                milestone.setMilestoneName(refMilestone.getName());
            }
        }

        return milestone;
    }
}
