package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.mapper.MilestoneMessageMapper;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.web.common.validator.PostProcessValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
@AllArgsConstructor
public class MessageApiImpl implements MessageApi {

    private static final String FLIGHT_SUBSCRIPTION = "Flight subscription for `{}`";
    private static final String LOG_FLIGHT_CANCELED = "Sending canceled flight for segment {} to dispatch. Reason = {}";
    private static final String LOG_SEND_UPDATED_SEGMENT_TO_Q_SHIP = "Sending updated segmentId: {} to QShip.";
    private static final String SEGMENT_CANCELLED = "Sending cancelled segment message {} with reason {}";
    private static final String LOG_QSHIP_SHIPMENT_JOURNEY = "Sending Shipment {} with Journey {} to QShip.";
    private static final String LOG_DSP_FAILED_JOURNEY = "Journey {} not sent to Dispatch. Reason: failed post process validation.";
    private static final String LOG_DSP_FAILED_SEGMENT = "Segment {} not sent to Dispatch. Reason: failed post process validation.";

    private PostProcessValidator<ShipmentJourney> journeyValidator;
    private PostProcessValidator<PackageJourneySegment> segmentValidator;
    private MessageService messageService;
    private ObjectMapper objectMapper;

    @Async("externalApiExecutor")
    @Override
    public void sendShipmentToQShip(Shipment shipment) {
        ShipmentJourney journey = shipment.getShipmentJourney();
        log.info(LOG_QSHIP_SHIPMENT_JOURNEY, shipment.getId(), journey.getJourneyId());
        messageService.sendShipmentToQShip(shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendShipmentWithJourneyToQShip(Shipment shipment, ShipmentJourney journey) {
        log.info(LOG_QSHIP_SHIPMENT_JOURNEY, shipment.getId(), journey.getJourneyId());
        messageService.sendShipmentWithJourneyToQShip(shipment, journey);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendUpdatedSegmentFromShipment(Shipment shipment, String segmentId) {
        log.info(LOG_SEND_UPDATED_SEGMENT_TO_Q_SHIP, segmentId);
        messageService.sendUpdatedSegmentFromShipment(shipment, segmentId);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendUpdatedSegmentFromShipment(ShipmentMessageDto shipmentMessageDto, PackageJourneySegment packageJourneySegment) {
        log.info(LOG_SEND_UPDATED_SEGMENT_TO_Q_SHIP, packageJourneySegment.getSegmentId());
        messageService.sendUpdatedSegmentFromShipment(shipmentMessageDto, packageJourneySegment);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendUpdatedSegmentFromShipment(Shipment shipment, PackageJourneySegment segment) {
        log.info(LOG_SEND_UPDATED_SEGMENT_TO_Q_SHIP, segment.getSegmentId());
        messageService.sendUpdatedSegmentFromShipment(shipment, segment);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendSegmentDispatch(List<Shipment> shipments, ShipmentJourney journey, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        if (!journeyValidator.isValid(journey)) {
            log.warn(LOG_DSP_FAILED_JOURNEY, journey.getJourneyId());
            return;
        }

        log.info("Sending multiple Shipments with Journey {} to Dispatch.", journey.getJourneyId());
        messageService.sendSegmentDispatch(shipments, journey, type, dspSegmentMsgUpdateSource);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendSegmentDispatch(Shipment shipment, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        ShipmentJourney journey = shipment.getShipmentJourney();
        if (!journeyValidator.isValid(journey)) {
            log.warn(LOG_DSP_FAILED_JOURNEY, journey.getJourneyId());
            return;
        }

        log.info("Sending Shipment(s) {} with Journey {} to Dispatch.", shipment.getId(), journey.getJourneyId());
        messageService.sendSegmentsDispatch(shipment, type, dspSegmentMsgUpdateSource);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendSegmentDispatch(Shipment shipment, PackageJourneySegment segment, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        if (!segmentValidator.isValid(segment)) {
            log.warn(LOG_DSP_FAILED_SEGMENT, segment.getSegmentId());
            return;
        }

        log.info("Sending Shipment {} with Segment {} to Dispatch.", shipment.getId(), segment.getSegmentId());
        messageService.sendSegmentsDispatch(shipment, segment, type, dspSegmentMsgUpdateSource);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendDispatchMilestoneError(MilestoneError milestoneError) {
        log.info("Sending Milestone error back to dispatch.");
        messageService.sendMilestoneError(milestoneError);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendUpdatedPackageDimensionsForShipment(Shipment shipment) {
        log.info("Sending Updated package dimensions for Shipment {} to other Products.", shipment.getId());
        messageService.sendPackageDimensions(shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public void subscribeFlight(FlightStatsRequest flight) {
        log.debug(FLIGHT_SUBSCRIPTION, flight);
        messageService.subscribeFlight(flight);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendDispatchCanceledFlight(Shipment shipment, String segmentId, String reason) {
        log.debug(LOG_FLIGHT_CANCELED, segmentId, reason);
        messageService.sendDispatchCanceled(shipment, segmentId, reason);
    }

    @Async("externalApiExecutor")
    @Override
    public void sendSegmentCancelled(Shipment shipment, String segmentId, String reason) {
        log.debug(SEGMENT_CANCELLED, segmentId, reason);
        messageService.sendDispatchCanceled(shipment, segmentId, reason);
    }

    @Async("externalApiExecutor")
    @Override
    public String sendMilestoneMessage(Shipment shipment, TriggeredFrom from) {
        String msg = null;
        MilestoneMessage milestoneMessage = null;
        if (isNull(shipment) ||
                isNull(shipment.getMilestone()) ||
                isNull(shipment.getShipmentJourney()) ||
                CollectionUtils.isEmpty(shipment.getShipmentJourney().getPackageJourneySegments())) {
            return null;
        }
        try {
            PackageJourneySegment segment = shipment.getShipmentJourney()
                    .getPackageJourneySegments()
                    .stream()
                    .filter(e -> e.getStatus() != SegmentStatus.COMPLETED).findFirst().orElse(null);
            if (nonNull(segment)) {
                milestoneMessage = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, shipment.getMilestone());
                milestoneMessage.setTriggeredFrom(from);
                messageService.sendMilestoneMessage(shipment, milestoneMessage, from);
                msg = objectMapper.writeValueAsString(milestoneMessage);
            }
        } catch (JsonProcessingException e) {
            log.error("Sending MilestoneMessage failed {}. Error message {} ", milestoneMessage, e.getMessage());
        }
        return msg;
    }

    @Async("externalApiExecutor")
    public String sendMilestoneMessage(Milestone milestone, Shipment shipment) {
        String msg = null;
        MilestoneMessage milestoneMessage = null;
        if (nonNull(milestone) && StringUtils.isNotEmpty(milestone.getShipmentId()) && StringUtils.isNotEmpty(milestone.getSegmentId())) {
            PackageJourneySegment segment = shipment
                    .getShipmentJourney()
                    .getPackageJourneySegments()
                    .stream()
                    .filter(e -> StringUtils.equals(e.getSegmentId(), milestone.getSegmentId())).findAny().orElse(null);
            if (nonNull(segment)) {
                milestoneMessage = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
                milestoneMessage.setTriggeredFrom(TriggeredFrom.DSP);
            }
            messageService.sendMilestoneMessage(shipment, milestoneMessage, TriggeredFrom.DSP);
            try {
                msg = objectMapper.writeValueAsString(milestoneMessage);
            } catch (JsonProcessingException e) {
                log.error("Sending MilestoneMessage failed {}. Error message {} ", milestoneMessage, e.getMessage());
            }
        }
        return msg;
    }

    @Async("externalApiExecutor")
    public void sendMilestoneMessage(Milestone milestone, Shipment shipment, PackageJourneySegment segment,
                                     TriggeredFrom from) {
        MilestoneMessage milestoneMessage = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, milestone);
        milestoneMessage.setTriggeredFrom(from);

        messageService.sendMilestoneMessage(shipment, milestoneMessage, from);
    }

    @Async("externalApiExecutor")
    public void sendFlightMilestoneMessage(Milestone milestone, ShipmentMessageDto shipmentMessageDto, PackageJourneySegment segment, TriggeredFrom source) {
        MilestoneMessage milestoneMessage = MilestoneMessageMapper.createFlightMilestoneMessage(shipmentMessageDto, segment, milestone);
        milestoneMessage.setTriggeredFrom(source);
        messageService.sendMilestoneMessage(milestoneMessage);
    }
}
