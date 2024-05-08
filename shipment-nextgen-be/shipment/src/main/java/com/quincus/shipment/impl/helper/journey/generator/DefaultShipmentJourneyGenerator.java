package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.mapper.OMLocationMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order
@Component
@AllArgsConstructor
public class DefaultShipmentJourneyGenerator implements ShipmentJourneyGenerator {
    private static final String DEFAULT_SEQUENCE = "0";
    private static final String DEFAULT_REF_ID = "0";

    @Override
    public ShipmentJourney generateShipmentJourney(Root omMessage) {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.PLANNED);
        shipmentJourney.addPackageJourneySegment(generateDefaultSegments(omMessage));
        return shipmentJourney;
    }

    private PackageJourneySegment generateDefaultSegments(Root omMessage) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setOrganizationId(omMessage.getOrganisationId());
        segment.setOpsType(omMessage.getOpsType());
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setRefId(DEFAULT_REF_ID);
        segment.setSequence(DEFAULT_SEQUENCE);
        segment.setPickUpTime(DateTimeUtil.toIsoDateTimeFormat(omMessage.getPickupStartTime()));
        segment.setPickUpCommitTime(DateTimeUtil.toIsoDateTimeFormat(omMessage.getPickupCommitTime()));
        segment.setDropOffTime(DateTimeUtil.toIsoDateTimeFormat(omMessage.getDeliveryStartTime()));
        segment.setDropOffCommitTime(DateTimeUtil.toIsoDateTimeFormat(omMessage.getDeliveryCommitTime()));
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        segment.setStartFacility(OMLocationMapper.mapLocationToFacility(omMessage.getOrigin().getId(), omMessage.getOrigin(), Shipment.ORIGIN_PROPERTY_NAME));
        segment.setEndFacility(OMLocationMapper.mapLocationToFacility(omMessage.getDestination().getId(), omMessage.getDestination(), Shipment.DESTINATION_PROPERTY_NAME));
        return segment;
    }
}
