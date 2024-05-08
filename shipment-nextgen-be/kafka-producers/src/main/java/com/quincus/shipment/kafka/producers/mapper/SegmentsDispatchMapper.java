package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.producers.message.dispatch.OrderMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipmentMsgPart;

import java.util.List;

public interface SegmentsDispatchMapper {

    SegmentsDispatchMessage mapJourneyAndShipmentsToSegmentsDispatchMessage(List<Shipment> shipmentDomains,
                                                                            ShipmentJourney journeyDomain);

    SegmentsDispatchMessage mapSegmentAndShipmentsToSegmentsDispatchMessage(List<Shipment> shipments,
                                                                            PackageJourneySegment segment);

    SegmentCancelMessage mapShipmentDomainToSegmentCancelMessage(Shipment shipmentDomain);

    OrderMsgPart mapOrderDomainToOrderMsgPart(Order orderDomain);

    ShipmentMsgPart mapShipmentDomainToShipmentMsgPart(Shipment shipmentDomain);

    SegmentMsgPart mapSegmentDomainToSegmentMsgPart(PackageJourneySegment segmentDomain, Order orderDomain);
}
