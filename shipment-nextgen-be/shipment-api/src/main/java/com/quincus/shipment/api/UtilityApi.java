package com.quincus.shipment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;

import java.util.List;

public interface UtilityApi {

    JsonNode getDspSegmentsDispatch(List<Shipment> shipmentList, ShipmentJourney journey, SegmentDispatchType type);

    List<String> getQshipSegment(Shipment shipment);

    void cleanUp(String entity, String id);

    String simulateMilestoneMessageFromDispatch(String dspPayload);

    MilestoneError simulateMilestoneMessageFromDispatchAndGetError(String dspPayload);

    JsonNode getDspSegmentsDispatchFromMilestone(String dspPayload);

    String simulateFlightEventMessageFromApiG(String apiGPayload);

    String getKafkaMessageForShipmentTrackingIdAndUpdatePackageDimensionResponse(String shipmentTrackingId, PackageDimensionUpdateResponse updatePackageDimensionResponse);

    List<String> sendFlightCanceledAndGetMessages(String apiGPayload);

    String checkFlightDelayedAndGetMessage(Shipment shipment);

    String unCacheSegmentLockoutLimePassed(String segmentId);

    Shipment cancelOnly(String id);

    Milestone getMilestoneFromDispatch(String dspPayload);

    Shipment updateAdditionalInfoNoSendToOtherProducts(ShipmentMilestoneOpsUpdateRequest infoRequest);
}
