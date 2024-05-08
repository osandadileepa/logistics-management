package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;

import java.util.List;

public interface ShipmentToQshipMapper {

    List<QshipSegmentMessage> mapShipmentDomainToQshipSegmentMessageList(Shipment shipmentDomain);

    List<QshipSegmentMessage> mapShipmentDomainToQshipSegmentMessageList(Shipment shipmentDomain, ShipmentJourney journeyDomain);

    QshipSegmentMessage mapSegmentDomainToQshipSegmentMessage(PackageJourneySegment segmentDomain, Shipment shipmentDomain);

    QshipSegmentMessage mapSegmentDomainToQshipSegmentMessage(PackageJourneySegment segmentDomain, ShipmentMessageDto shipmentMessageDto);
}
