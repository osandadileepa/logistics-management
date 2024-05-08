package com.quincus.shipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/utilities")
@Tag(name = "utilities", description = "A utility endpoints for simulation of QShip, Milestone, Package Info and DB clean up.")
public interface UtilityController {

    @PostMapping("/simulate/dsp/receive-milestone")
    @Operation(summary = "Simulate Milestone Message Utility API", description = "Simulate receiving a milestone message from DSP.", tags = "utilities")
    ResponseEntity<String> simulateMilestoneMessageFromDispatch(@RequestBody final String dspPayload);

    @PostMapping("/simulate/dsp/receive-milestone-with-error")
    @Operation(summary = "Simulate Milestone with error Utility API", description = "Simulate a milestone message from DSP AND get Errors if any.", tags = "utilities")
    ResponseEntity<MilestoneError> simulateMilestoneMessageFromDispatchWithError(@RequestBody final String dspPayload);

    @PostMapping("/simulate/dsp/send-segment")
    @Operation(summary = "Create DSP Segment Dispatch Utility API", description = "Creates DSP segment dispatch.", tags = "utilities")
    Response<SegmentsDispatchMessage> createDspSegmentsDispatch(@Valid @RequestBody final Request<Object> request) throws JsonProcessingException;

    @PostMapping("/simulate/dsp/receive-milestone-and-get-segment-dispatch")
    @Operation(summary = "Create DSP Segment Dispatch from Milestone Utility API", description = "Creates DSP segment dispatch from milestone.", tags = "utilities")
    Response<SegmentsDispatchMessage> createDspSegmentsDispatchFromMilestone(@RequestBody final String dspPayload) throws JsonProcessingException;

    @PostMapping("/simulate/qship/send-segment")
    @Operation(summary = "Create QShip Segment Utility API", description = "Creates QShip Segments", tags = "utilities")
    Response<List<JsonNode>> createQshipSegment(@Valid @RequestBody final Request<Object> request) throws JsonProcessingException;

    @PutMapping("/simulate/get-by-tracking-id/{shipment_tracking_id}/package-dimension")
    @Operation(summary = "Update Shipment Package Utility API", description = "Updates package dimension", tags = "utilities")
    Response<String> updateShipmentPackageDimension(@PathVariable("shipment_tracking_id") final String shipmentTrackingId,
                                                    @Valid @RequestBody final Request<PackageDimensionUpdateRequest> request);

    @PostMapping("/simulate/apig/receive-flight-event")
    @Operation(summary = "Simulate FlightEvent Message Utility API", description = "Simulate receiving a flightEvent message from API-G.", tags = "utilities")
    ResponseEntity<String> simulateFlightEventMessageFromApiG(@RequestBody final String apiGPayload);

    @PostMapping("/simulate/apig/subscribe-flight")
    @Operation(summary = "Flight Subscription Utility API", description = "Subscribe a flight.", tags = "utilities")
    void subscribeFlight(@Valid @RequestBody final Request<FlightStatsRequest> request);

    @PostMapping("/simulate/apig/update-order-progress")
    @Operation(summary = "Update Order Progress Webhook Utility API", description = "Update Order Progress From Milestone Update", tags = "utilities")
    Response<ApiGatewayWebhookResponse> updateOrderProgress(@RequestBody final String milestonePayload) throws JsonProcessingException;

    @PostMapping("/simulate/apig/update-order-additional-charges/{costId}")
    @Operation(summary = "Update Additional Charges Webhook Utility API", description = "Update Additional Charges From Cost", tags = "utilities")
    Response<List<ApiGatewayWebhookResponse>> updateOrderAdditionalCharges(@PathVariable("costId") final String costId);

    @PostMapping("/simulate/dsp/notify-canceled-flight-and-get-message")
    @Operation(summary = "Send Flight Canceled Utility API", description = "Send flight canceled and get message.", tags = "utilities")
    ResponseEntity<List<String>> simulateSendFlightCancelled(@RequestBody final String apiGPayload) throws JsonProcessingException;

    @PostMapping("/simulate/dsp/check-lockout-time-and-get-message")
    @Operation(summary = "Check Flight Delay Utility API", description = "Check flight delay via lockout time and get message.", tags = "utilities")
    ResponseEntity<String> checkFlightDelayed(@Valid @RequestBody final Request<Shipment> request) throws JsonProcessingException;

    @DeleteMapping("/simulate/dsp/un-cache-segment-lockout-time-passed")
    @Operation(summary = "Un-cache Segment Utility API", description = "Un-caches a segment that previously missed the lockout time.", tags = "utilities")
    ResponseEntity<String> unCacheSegmentLockoutTimePassed(@NotBlank @PathVariable("segment_id") final String segmentId);

    @PostMapping("/simulate/apig/assign-vendor-details")
    @Operation(summary = "Assign Vendor Details Webhook Utility API", description = "Assign Vendor Details From Milestone Update", tags = "utilities")
    Response<ApiGatewayWebhookResponse> assignVendorDetails(@RequestBody final String milestonePayload) throws JsonProcessingException;

    @PostMapping("/simulate/milestone-update-from-shp-or-om")
    @Operation(summary = "Send Milestone Update FROM SHP/OM Utility API", description = "Send Milestone Update FROM SHP/OM", tags = "utilities")
    Response<MilestoneMessage> sendMilestoneUpdateFromSHPOrOM(@RequestBody final Request<Shipment> request) throws JsonProcessingException;

    @PostMapping("/simulate/milestone-update-from-dsp")
    @Operation(summary = "Send Milestone Update FROM DSP Utility API", description = "Send Milestone Update DSP", tags = "utilities")
    Response<MilestoneMessage> sendMilestoneUpdateFromDSP(@RequestBody final Request<Milestone> request) throws JsonProcessingException;

    @PostMapping("/simulate/apig/check-in")
    @Operation(summary = "Check In Webhook Utility API", description = "Check In From Milestone Update", tags = "utilities")
    Response<ApiGatewayWebhookResponse> checkIn(@RequestBody final String milestonePayload) throws JsonProcessingException;

    @PatchMapping("/simulate/shipment/cancel/{id}")
    @Operation(summary = "Cancel Shipment then get outgoing milestone API", description = "Cancel an existing shipment then return the outgoing milestone payload.", tags = "utilities")
    Response<MilestoneMessage> cancelAndGetShipmentMilestone(@PathVariable("id") final String id);

    @PostMapping("/simulate/dsp/receive-milestone-and-get-shipment-milestone")
    @Operation(summary = "Simulate Milestone Message Utility then get outgoing milestone API", description = "Simulate receiving a milestone message from DSP then return the outgoing milestone payload.", tags = "utilities")
    Response<MilestoneMessage> dspMilestoneToShipmentMilestone(@RequestBody final String dspPayload);

    @PatchMapping("/simulate/shipment/{shipment_tracking_id}/milestone-and-additional-info")
    @Operation(summary = "Update Shipment Milestone Status, Notes and Attachments then get outgoing milestone API", description = "Update a shipment’s milestone status, notes, and attachments based on shipment tracking id then return the outgoing milestone payload.", tags = "utilities")
    Response<MilestoneMessage> updateMilestoneAddtlInfoAndGetMilestone(
            @PathVariable("shipment_tracking_id") final String shipmentTrackingId, @Valid @RequestBody final Request<ShipmentMilestoneOpsUpdateRequest> additionalDetailsRequest);
}
