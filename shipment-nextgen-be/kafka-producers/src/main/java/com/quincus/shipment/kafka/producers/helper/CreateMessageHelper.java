package com.quincus.shipment.kafka.producers.helper;

import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.mapper.FlightStatsMessageMapper;
import com.quincus.shipment.kafka.producers.mapper.SegmentsDispatchMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToOrderMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToPackageDimensionsMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToQshipMapper;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import com.quincus.shipment.kafka.producers.message.ShipShipmentPathMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class CreateMessageHelper {

    private final ShipmentToOrderMapper shipmentToOrderMapper;
    private final ShipmentToQshipMapper shipmentToQshipMapper;
    private final ShipmentToPackageDimensionsMapper shipmentToPackageDimensionsMapper;
    private final SegmentsDispatchMapper segmentsDispatchMapper;
    private final FlightStatsMessageMapper flightStatsMessageMapper;

    public PackageDimensionsMessage createShipmentPackageDimensions(Shipment shipment) {
        return shipmentToPackageDimensionsMapper.mapShipmentToPackageDimensionsMessage(shipment);
    }

    public MilestoneMessage createMilestoneMessage(Shipment shipment) {
        return shipmentToOrderMapper.mapShipmentDomainToMilestoneMessage(shipment);
    }

    public ShipShipmentPathMessage createShipShipmentPathMessage(Shipment shipment) {
        return shipmentToOrderMapper.mapShipmentDomainToShipmentPathMessage(shipment);
    }

    public QshipSegmentMessage createQshipSegmentMessage(Shipment shipment, PackageJourneySegment segment) {
        return shipmentToQshipMapper.mapSegmentDomainToQshipSegmentMessage(segment, shipment);
    }

    public QshipSegmentMessage createQshipSegmentMessage(ShipmentMessageDto shipmentMessageDto, PackageJourneySegment segment) {
        return shipmentToQshipMapper.mapSegmentDomainToQshipSegmentMessage(segment, shipmentMessageDto);
    }

    public List<QshipSegmentMessage> createQshipSegmentMessageList(Shipment shipment) {
        return shipmentToQshipMapper.mapShipmentDomainToQshipSegmentMessageList(shipment);
    }

    public List<QshipSegmentMessage> createQshipSegmentMessageList(Shipment shipment, ShipmentJourney journey) {
        return shipmentToQshipMapper.mapShipmentDomainToQshipSegmentMessageList(shipment, journey);
    }

    public ShipmentCancelMessage createShipmentCancelMessage(Shipment shipment) {
        return shipmentToOrderMapper.mapShipmentDomainToShipmentCancelMessage(shipment);
    }

    public SegmentsDispatchMessage createSegmentsDispatchMessage(List<Shipment> shipments, ShipmentJourney journey,
                                                                 SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        SegmentsDispatchMessage message = segmentsDispatchMapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(shipments,
                journey);
        //Message ID is for reference only
        message.setId(UUID.randomUUID().toString());
        message.setType(type);
        message.setUpdateSource(dspSegmentMsgUpdateSource);

        return message;
    }

    public SegmentsDispatchMessage createSegmentsDispatchMessage(List<Shipment> shipments,
                                                                 PackageJourneySegment updatedSegment,
                                                                 SegmentDispatchType type,
                                                                 DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        SegmentsDispatchMessage message = segmentsDispatchMapper.mapSegmentAndShipmentsToSegmentsDispatchMessage(shipments,
                updatedSegment);
        //Message ID is for reference only
        message.setId(UUID.randomUUID().toString());
        message.setUpdateSource(dspSegmentMsgUpdateSource);
        message.setType(type);

        return message;
    }

    public FlightStatsMessage createFlightSubscriptionMessage(FlightStatsRequest flight, String uuid) {
        FlightEventPayloadMessage flightEventPayloadMessage = flightStatsMessageMapper.mapFlightToEventPayloadMessage(flight);
        return flightStatsMessageMapper.mapToFlightStatsMessage(flightEventPayloadMessage, uuid);
    }

    public SegmentCancelMessage createSegmentCancelMessage(Shipment shipment, String segmentId, String reason) {
        SegmentCancelMessage message = segmentsDispatchMapper.mapShipmentDomainToSegmentCancelMessage(shipment);
        message.setSegmentId(segmentId);
        message.setReason(reason);
        return message;
    }
}
