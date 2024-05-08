package com.quincus.shipment.impl.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.shipment.api.AlertApi;
import com.quincus.shipment.api.FlightStatsApi;
import com.quincus.shipment.api.MilestoneApi;
import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.UtilityApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.service.CleanUpService;
import com.quincus.shipment.impl.service.FlightService;
import com.quincus.shipment.impl.service.FlightStatsEventService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.shipment.impl.service.ShipmentProcessingService;
import com.quincus.shipment.impl.service.ShipmentService;
import com.quincus.shipment.impl.service.scheduler.ShipmentLockoutTimeScheduler;
import com.quincus.shipment.kafka.producers.MessageService;
import com.quincus.shipment.kafka.producers.helper.CreateMessageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@AllArgsConstructor
public class UtilityApiImpl implements UtilityApi {
    private static final String ERR_OBJ_SERIALIZATION = "Error occurred while serializing object to string.";
    private static final String ERROR_FAILED_CONSUMING_MESSAGE_FROM_DISPATCH = "Something went wrong while consuming dispatch message.";
    private static final String FIELD_ERROR = "error";
    private final MessageService messageService;
    private final ShipmentService shipmentService;
    private final PackageJourneySegmentService segmentService;
    private final MilestoneService milestoneService;
    private final FlightStatsEventService flightStatsEventService;
    private final FlightService flightService;
    private final CleanUpService cleanUpService;
    private final ShipmentLockoutTimeScheduler shipmentLockoutTimeScheduler;
    private final ObjectMapper objectMapper;
    private final FlightStatsApi flightStatsApi;
    private final ShipmentApi shipmentApi;
    private final MilestoneApi milestoneApi;
    private final QPortalService qPortalService;
    private final MilestonePostProcessApi milestonePostProcessApi;
    private CreateMessageHelper messageHelper;
    private ShipmentRepository shipmentRepository;
    private ShipmentProcessingService shipmentProcessingService;
    private AlertApi alertApi;

    @Override
    public JsonNode getDspSegmentsDispatch(List<Shipment> shipmentList, ShipmentJourney journey,
                                           SegmentDispatchType type) {
        String segmentDispatchMsg = messageService.generateSegmentsDispatchMessage(shipmentList, journey, type, DspSegmentMsgUpdateSource.CLIENT);
        JsonNode segmentDispatchJson;
        try {
            segmentDispatchJson = objectMapper.readTree(segmentDispatchMsg);
        } catch (JsonProcessingException e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put(FIELD_ERROR, e.getMessage());
            segmentDispatchJson = error;
        }
        return segmentDispatchJson;
    }

    @Override
    public List<String> getQshipSegment(Shipment shipment) {
        return messageService.getQshipSegment(shipment);
    }

    @Override
    public void cleanUp(String entity, String id) {
        cleanUpService.cleanUpByEntityAndId(entity, id);
    }

    @Override
    public String simulateMilestoneMessageFromDispatch(String dspPayload) {
        String uuid = UUID.randomUUID().toString();

        Milestone milestone = shipmentApi.receiveMilestoneMessageFromDispatch(dspPayload, uuid);
        if (milestone != null) {
            Shipment shipment = shipmentApi.find(milestone.getShipmentId());
            milestonePostProcessApi.createAndSendShipmentMilestone(milestone, shipment);
            try {
                milestonePostProcessApi.createAndSendAPIGWebhooks(milestone, shipment);
            } catch (Exception e) {
                return ERROR_FAILED_CONSUMING_MESSAGE_FROM_DISPATCH;
            }
            if (milestoneApi.isFailedStatusCode(milestone.getMilestoneCode())) {
                alertApi.createPickupDeliveryFailedAlert(milestone.getShipmentId());
            }
            milestonePostProcessApi.createAndSendSegmentDispatch(milestone, shipment);
            milestonePostProcessApi.createAndSendQShipSegment(milestone, shipment);
            milestonePostProcessApi.createAndSendNotification(milestone, shipment);
        }
        return "Simulated receiving a milestone message from DSP";
    }

    @Override
    public MilestoneError simulateMilestoneMessageFromDispatchAndGetError(String dspPayload) {
        MilestoneError milestoneError = new MilestoneError();
        String utilUuid = "from-utility-api";
        Milestone dspMilestone = milestoneService.initializeDispatchMilestone(dspPayload, utilUuid);
        milestoneError.setMilestone(milestoneService.getDispatchMessageJson(dspPayload));
        try {
            milestoneService.validateDispatchMilestone(dspMilestone, utilUuid);
        } catch (ConstraintViolationException e) {
            List<String> errorList = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();
            milestoneError.setErrors(errorList);
        } catch (Exception e) {
            milestoneError.setErrors(List.of(e.getMessage()));
        }
        return milestoneError;
    }

    @Override
    public JsonNode getDspSegmentsDispatchFromMilestone(String dspPayload) {
        Milestone milestone = getMilestoneFromDispatch(dspPayload);
        if (milestone == null) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put(FIELD_ERROR, "error occurred while creating Milestone object from payload.");
            return errorNode;
        }

        Shipment shipment = shipmentApi.find(milestone.getShipmentId());
        if (shipment == null) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put(FIELD_ERROR, "milestone payload does not refer to an existing shipment.");
            return errorNode;
        }

        String msg = messageService.generateSegmentsDispatchMessage(List.of(shipment), shipment.getShipmentJourney(),
                SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);

        try {
            return objectMapper.readTree(msg);
        } catch (JsonProcessingException e) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put(FIELD_ERROR, e.getMessage());
            return errorNode;
        }
    }

    @Override
    public String getKafkaMessageForShipmentTrackingIdAndUpdatePackageDimensionResponse(String shipmentTrackingId, PackageDimensionUpdateResponse updatePackageDimensionResponse) {
        Optional<ShipmentEntity> optionalShipmentEntity = shipmentRepository.findByShipmentTrackingIdAndOrganizationId(
                shipmentTrackingId, updatePackageDimensionResponse.getOrganizationId());
        Shipment shipment;
        String response;
        if (optionalShipmentEntity.isPresent()) {
            shipment = ShipmentMapper.mapEntityToDomain(shipmentRepository.save(optionalShipmentEntity.get()), objectMapper);
            response = objectMapper.convertValue(messageHelper.createShipmentPackageDimensions(shipment),
                    JsonNode.class).toString();
        } else {
            response = "Shipment tracking id not found";
        }
        return response;
    }

    @Override
    public String simulateFlightEventMessageFromApiG(String apiGPayload) {
        String uuid = UUID.randomUUID().toString();
        flightStatsApi.receiveFlightStatsMessage(apiGPayload, uuid);
        return "Simulated receiving a flight status message from API-G";
    }

    @Override
    public List<String> sendFlightCanceledAndGetMessages(String apiGPayload) {
        String messageTransactionId = UUID.randomUUID().toString();
        Flight flight = flightStatsEventService.toFlight(apiGPayload, messageTransactionId);
        if ((flight == null) || (FlightEventName.CANCELLED) != flight.getFlightStatus().getEventName()) {
            return Collections.emptyList();
        }
        flightService.createOrUpdate(flight);
        List<PackageJourneySegmentEntity> segmentEntities = getSegmentsWithFlight(flight);
        List<String> segmentIdList = segmentEntities.stream().map(PackageJourneySegmentEntity::getId).toList();
        List<Shipment> relatedShipments = shipmentProcessingService.findShipmentsFromSegmentList(segmentIdList);

        List<String> flightCancelMessage = new ArrayList<>();
        for (int i = 0; i < segmentEntities.size(); i++) {
            PackageJourneySegmentEntity segmentEntity = segmentEntities.get(i);
            PackageJourneySegment segment = PackageJourneySegmentMapper.mapEntityToDomain(false, segmentEntity, i, segmentEntities.size());
            Optional<Shipment> shipmentRef = ShipmentUtil.filterShipmentFromSegmentId(relatedShipments, segment.getSegmentId());
            if (shipmentRef.isPresent()) {
                try {
                    flightCancelMessage.add(objectMapper.writeValueAsString(messageHelper.createSegmentCancelMessage(shipmentRef.get(), segmentEntity.getId(), "flight canceled")));
                } catch (JsonProcessingException e) {
                    log.warn(ERR_OBJ_SERIALIZATION, e);
                }
            }
        }
        if (flightCancelMessage.isEmpty()) {
            return Collections.emptyList();
        }

        return flightCancelMessage;
    }

    @Override
    public String checkFlightDelayedAndGetMessage(Shipment shipment) {
        PackageJourneySegment segmentEntity = shipmentLockoutTimeScheduler.getAirSegmentLockoutTimeMissed(shipment);
        if (segmentEntity == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(messageHelper.createSegmentCancelMessage(shipment,
                    segmentEntity.getSegmentId(), "flight canceled"));
        } catch (JsonProcessingException e) {
            log.warn(ERR_OBJ_SERIALIZATION, e);
        }

        return null;
    }

    @Override
    public String unCacheSegmentLockoutLimePassed(String segmentId) {
        segmentService.unCacheLockoutTimePassedSegment(segmentId);
        return String.format("Triggered un-cache segment with ID: %s", segmentId);
    }

    @Override
    public Shipment cancelOnly(String id) {
        return shipmentService.cancelShipmentById(id);
    }

    @Override
    public Milestone getMilestoneFromDispatch(String dspPayload) {
        String uuid = UUID.randomUUID().toString();
        return shipmentApi.receiveMilestoneMessageFromDispatch(dspPayload, uuid);
    }

    @Override
    public Shipment updateAdditionalInfoNoSendToOtherProducts(ShipmentMilestoneOpsUpdateRequest infoRequest) {
        ShipmentEntity shipmentEntity = shipmentService.validateMilestoneInfoAndPrepareShipment(infoRequest);
        PackageJourneySegmentEntity currentActiveSegment = ShipmentUtil.getActiveSegmentEntity(shipmentEntity);
        Milestone currentMilestone = milestoneService.createMilestoneFromOpsUpdate(shipmentEntity, currentActiveSegment, infoRequest);
        milestoneService.enrichMilestoneWithQPortalInfo(currentMilestone, qPortalService.listMilestones());
        Shipment updatedShipment = ShipmentMapper.mapEntityToDomain(shipmentRepository.save(shipmentEntity), objectMapper);
        updatedShipment.setMilestone(currentMilestone);
        PackageJourneySegment segment = ShipmentUtil.getActiveSegment(updatedShipment);
        if (nonNull(segment)) {
            Milestone milestone = updatedShipment.getMilestone();
            if (milestone.getAdditionalInfo() == null) {
                milestone.setAdditionalInfo(new MilestoneAdditionalInfo());
            }
            milestone.getAdditionalInfo().setRemarks(infoRequest.getNotes());
            milestone.setSegmentId(segment.getSegmentId());
            segmentService.updateSegmentStatusByMilestoneEvent(updatedShipment.getMilestone(), null);
        }
        return updatedShipment;
    }

    private List<PackageJourneySegmentEntity> getSegmentsWithFlight(Flight flight) {
        if (flight == null) {
            return Collections.emptyList();
        }
        return segmentService.findSegmentsWithFlightDetails(flight.getCarrier(),
                flight.getFlightNumber(), flight.getDepartureDate(), flight.getOrigin(),
                flight.getDestination(), flight.getFlightId());
    }
}