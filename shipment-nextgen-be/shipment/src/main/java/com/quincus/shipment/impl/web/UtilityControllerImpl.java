package com.quincus.shipment.impl.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.ext.annotation.Utility;
import com.quincus.order.api.OrderApi;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.UtilityController;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.UtilityApi;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.CostService;
import com.quincus.shipment.kafka.producers.mapper.MapperUtil;
import com.quincus.shipment.kafka.producers.mapper.MilestoneMessageMapper;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@AllArgsConstructor
@PreAuthorize("hasAuthority('SUPER-ADMIN')")
public class UtilityControllerImpl implements UtilityController {
    private final UtilityApi utilityApi;
    private final OrderApi orderApi;
    private final ShipmentApi shipmentApi;
    private final MessageApi messageApi;
    private final ObjectMapper objectMapper;
    private final ApiGatewayApi apiGatewayApi;
    private final MilestoneMapper milestoneMapper;
    private final UserDetailsProvider userDetailsProvider;
    private final CostService costService;
    private final UpdateShipmentHelper updateShipmentHelper;

    @Override
    @LogExecutionTime
    public ResponseEntity<String> simulateMilestoneMessageFromDispatch(final String dspPayload) {
        return ResponseEntity.ok(utilityApi.simulateMilestoneMessageFromDispatch(dspPayload));
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<MilestoneError> simulateMilestoneMessageFromDispatchWithError(final String dspPayload) {
        return ResponseEntity.ok(utilityApi.simulateMilestoneMessageFromDispatchAndGetError(dspPayload));
    }

    @Utility
    @Override
    @LogExecutionTime
    public Response<SegmentsDispatchMessage> createDspSegmentsDispatch(final Request<Object> request) {
        try {
            String orderPayload = objectMapper.writeValueAsString(request.getData());
            List<Shipment> shipmentList = orderApi.createOrUpdateShipmentsLocal(orderPayload, updateShipmentHelper.isSegmentsUpdated(orderPayload));

            SegmentDispatchType segmentDispatchType;
            if (shipmentList.stream().anyMatch(Shipment::isSegmentsUpdatedFromSource)) {
                segmentDispatchType = SegmentDispatchType.JOURNEY_UPDATED;
            } else if (shipmentList.stream().anyMatch(Shipment::isUpdated)) {
                segmentDispatchType = SegmentDispatchType.SHIPMENT_UPDATED;
            } else {
                segmentDispatchType = SegmentDispatchType.SHIPMENT_CREATED;
            }

            ShipmentJourney journey = shipmentList.get(0).getShipmentJourney();
            JsonNode dspSegmentDispatch = utilityApi.getDspSegmentsDispatch(shipmentList, journey, segmentDispatchType);
            return new Response<>(objectMapper.convertValue(dspSegmentDispatch, SegmentsDispatchMessage.class));
        } catch (JsonProcessingException e) {
            throw new QuincusException(e.getMessage(), e);
        }
    }

    @Override
    @LogExecutionTime
    public Response<SegmentsDispatchMessage> createDspSegmentsDispatchFromMilestone(final String dspPayload) {
        JsonNode dspSegmentDispatch = utilityApi.getDspSegmentsDispatchFromMilestone(dspPayload);
        return new Response<>(objectMapper.convertValue(dspSegmentDispatch, SegmentsDispatchMessage.class));
    }

    @Utility
    @Override
    @LogExecutionTime
    public Response<List<JsonNode>> createQshipSegment(final Request<Object> request) {
        try {
            String orderPayload = objectMapper.writeValueAsString(request.getData());
            List<Shipment> shipments = orderApi.createOrUpdateShipmentsLocal(orderPayload, updateShipmentHelper.isSegmentsUpdated(orderPayload));
            List<JsonNode> qshipSegmentMessages = shipments.stream()
                    .flatMap(shipment -> utilityApi.getQshipSegment(shipment).stream())
                    .map(json -> MapperUtil.readRawJson(json, objectMapper))
                    .toList();
            return new Response<>(qshipSegmentMessages);
        } catch (JsonProcessingException e) {
            throw new QuincusException(e.getMessage(), e);
        }
    }

    @Override
    @LogExecutionTime
    public Response<String> updateShipmentPackageDimension(final String shipmentTrackingId,
                                                           final Request<PackageDimensionUpdateRequest> request) {
        request.getData().setShipmentTrackingId(shipmentTrackingId);
        PackageDimensionUpdateResponse updatePackageDimensionResponse = shipmentApi.updateShipmentPackageDimension(request.getData());
        return new Response<>(utilityApi.getKafkaMessageForShipmentTrackingIdAndUpdatePackageDimensionResponse(
                shipmentTrackingId,
                updatePackageDimensionResponse)
        );
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<String> simulateFlightEventMessageFromApiG(String apiGPayload) {
        return ResponseEntity.ok(utilityApi.simulateFlightEventMessageFromApiG(apiGPayload));
    }

    @Override
    @LogExecutionTime
    public void subscribeFlight(final Request<FlightStatsRequest> request) {
        messageApi.subscribeFlight(request.getData());
    }

    @Override
    @LogExecutionTime
    public Response<ApiGatewayWebhookResponse> updateOrderProgress(final String milestonePayload) throws JsonProcessingException {
        Milestone milestone = milestoneMapper.convertMessageToDomain(milestonePayload);
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        Shipment shipment = shipmentApi.findAndCheckLocationPermission(milestone.getShipmentId());
        return new Response<>(apiGatewayApi.sendUpdateOrderProgress(shipment, milestone));
    }

    @Override
    @LogExecutionTime
    public Response<List<ApiGatewayWebhookResponse>> updateOrderAdditionalCharges(final String costId) {
        Cost cost = costService.find(costId);
        return new Response<>(apiGatewayApi.sendUpdateOrderAdditionalCharges(cost));
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<List<String>> simulateSendFlightCancelled(final String apiGPayload) {
        List<String> messageList = utilityApi.sendFlightCanceledAndGetMessages(apiGPayload);
        if (!CollectionUtils.isEmpty(messageList)) {
            return ResponseEntity.ok(messageList);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<String> checkFlightDelayed(final Request<Shipment> request) {
        Shipment shipment = request.getData();
        String message = utilityApi.checkFlightDelayedAndGetMessage(shipment);
        if (message != null) {
            return ResponseEntity.ok(message);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<String> unCacheSegmentLockoutTimePassed(String segmentId) {
        String message = utilityApi.unCacheSegmentLockoutLimePassed(segmentId);
        if (message != null) {
            return ResponseEntity.ok(message);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    @LogExecutionTime
    public Response<ApiGatewayWebhookResponse> assignVendorDetails(final String milestonePayload) throws JsonProcessingException {
        Milestone milestone = milestoneMapper.convertMessageToDomain(milestonePayload);
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        Shipment shipment = shipmentApi.findAndCheckLocationPermission(milestone.getShipmentId());
        return new Response<>(apiGatewayApi.sendAssignVendorDetails(shipment, milestone));
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneMessage> sendMilestoneUpdateFromSHPOrOM(Request<Shipment> request) throws JsonProcessingException {
        Shipment shipment = request.getData();
        boolean hasMatch = Stream.of(Root.STATUS_CANCELLED, Root.STATUS_CREATED)
                .anyMatch(e -> StringUtils.equalsIgnoreCase(shipment.getOrder().getStatus(), e));
        TriggeredFrom from = hasMatch ? TriggeredFrom.OM : TriggeredFrom.SHP;
        MilestoneMessage message = objectMapper.readValue(messageApi.sendMilestoneMessage(shipment, from), MilestoneMessage.class);
        return new Response<>(message);
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneMessage> sendMilestoneUpdateFromDSP(Request<Milestone> request) throws JsonProcessingException {
        Milestone milestone = request.getData();
        Shipment shipment = shipmentApi.findAndCheckLocationPermission(milestone.getShipmentId());
        MilestoneMessage message = objectMapper.readValue(messageApi.sendMilestoneMessage(milestone, shipment), MilestoneMessage.class);
        return new Response<>(message);
    }

    @Override
    @LogExecutionTime
    public Response<ApiGatewayWebhookResponse> checkIn(final String milestonePayload) throws JsonProcessingException {
        Milestone milestone = milestoneMapper.convertMessageToDomain(milestonePayload);
        milestone.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        Shipment shipment = shipmentApi.findAndCheckLocationPermission(milestone.getShipmentId());
        return new Response<>(apiGatewayApi.sendCheckInDetails(shipment, milestone));
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneMessage> cancelAndGetShipmentMilestone(final String id) {
        Shipment canceledShipment = utilityApi.cancelOnly(id);
        MilestoneMessage milestoneMessage = (canceledShipment.getMilestone() != null) ? createMilestoneMessageFromShipment(canceledShipment) : new MilestoneMessage();
        return new Response<>(milestoneMessage);
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneMessage> dspMilestoneToShipmentMilestone(final String dspPayload) {
        Milestone milestone = utilityApi.getMilestoneFromDispatch(dspPayload);
        Shipment shipment = shipmentApi.find(milestone.getShipmentId());
        MilestoneMessage milestoneMessage = createMilestoneMessageFromShipment(shipment);
        return new Response<>(milestoneMessage);
    }

    @Override
    @LogExecutionTime
    public Response<MilestoneMessage> updateMilestoneAddtlInfoAndGetMilestone(final String shipmentTrackingId,
                                                                              final Request<ShipmentMilestoneOpsUpdateRequest> additionalDetailsRequest) {
        additionalDetailsRequest.getData().setShipmentTrackingId(shipmentTrackingId);
        Shipment shipment = utilityApi.updateAdditionalInfoNoSendToOtherProducts(additionalDetailsRequest.getData());
        MilestoneMessage milestoneMessage = createMilestoneMessageFromShipment(shipment);
        return new Response<>(milestoneMessage);
    }

    private MilestoneMessage createMilestoneMessageFromShipment(Shipment shipment) {
        PackageJourneySegment segment = ShipmentUtil.getActiveSegment(shipment);
        MilestoneMessage milestoneMessage = null;
        if (segment != null) {
            milestoneMessage = MilestoneMessageMapper.createMilestoneMessage(shipment, segment, shipment.getMilestone());
            milestoneMessage.setTriggeredFrom(TriggeredFrom.SHP);
        }
        return milestoneMessage;
    }
}
