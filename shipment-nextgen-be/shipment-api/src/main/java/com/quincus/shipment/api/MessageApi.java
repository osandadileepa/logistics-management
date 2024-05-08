package com.quincus.shipment.api;

import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;

import java.util.List;

public interface MessageApi {

    void sendShipmentToQShip(Shipment shipment);

    void sendShipmentWithJourneyToQShip(Shipment shipment, ShipmentJourney journey);

    void sendUpdatedSegmentFromShipment(Shipment shipment, String segmentId);

    void sendUpdatedSegmentFromShipment(ShipmentMessageDto shipmentMessageDto, PackageJourneySegment segment);

    void sendUpdatedSegmentFromShipment(Shipment shipment, PackageJourneySegment segment);

    void sendSegmentDispatch(List<Shipment> shipments, ShipmentJourney journey, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource);

    void sendSegmentDispatch(Shipment shipment, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource);

    void sendSegmentDispatch(Shipment shipment, PackageJourneySegment segment, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource);

    void sendDispatchMilestoneError(MilestoneError milestoneError);

    void subscribeFlight(FlightStatsRequest flight);

    void sendUpdatedPackageDimensionsForShipment(Shipment shipment);

    void sendDispatchCanceledFlight(Shipment shipment, String segmentId, String reason);

    void sendSegmentCancelled(Shipment shipment, String segmentId, String reason);

    String sendMilestoneMessage(Shipment shipment, TriggeredFrom from);

    String sendMilestoneMessage(Milestone milestone, Shipment shipment);

    void sendMilestoneMessage(Milestone milestone, Shipment shipment, PackageJourneySegment segment, TriggeredFrom from);

    void sendFlightMilestoneMessage(Milestone milestone, ShipmentMessageDto shipmentMessageDto, PackageJourneySegment packageJourneySegment, TriggeredFrom from);

}
