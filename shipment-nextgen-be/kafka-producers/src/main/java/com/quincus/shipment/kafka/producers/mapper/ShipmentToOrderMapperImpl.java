package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import com.quincus.shipment.kafka.producers.message.ShipShipmentPathMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentPathMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class ShipmentToOrderMapperImpl implements ShipmentToOrderMapper {
    @Override
    public ShipShipmentPathMessage mapShipmentDomainToShipmentPathMessage(Shipment shipmentDomain) {
        if (isNull(shipmentDomain)) {
            return null;
        }
        ShipShipmentPathMessage shipmentPathMessage = new ShipShipmentPathMessage();
        shipmentPathMessage.setId(shipmentDomain.getId());
        Organization organizationDomain = shipmentDomain.getOrganization();
        shipmentPathMessage.setOrganizationId(organizationDomain.getId());
        Order orderDomain = shipmentDomain.getOrder();
        shipmentPathMessage.setOrderId(orderDomain.getId());
        ShipmentJourney shipmentJourneyDomain = shipmentDomain.getShipmentJourney();
        if (isNull(shipmentJourneyDomain)) {
            shipmentPathMessage.setShipmentPath(Collections.emptyList());

            return shipmentPathMessage;
        }
        List<ShipmentPathMessage> shipmentPathList = new ArrayList<>();
        for (PackageJourneySegment journeySegmentDomain : shipmentJourneyDomain.getPackageJourneySegments()) {
            ShipmentPathMessage shipmentPath = new ShipmentPathMessage();
            shipmentPath.setId(journeySegmentDomain.getSegmentId());
            Facility startFacilityDomain = journeySegmentDomain.getStartFacility();
            shipmentPath.setHubId(startFacilityDomain.getId());
            if (nonNull(journeySegmentDomain.getPartner())) {
                shipmentPath.setPartnerId(journeySegmentDomain.getPartner().getId());
            }
            shipmentPath.setTransportType(MapperUtil.getValueFromEnum(journeySegmentDomain.getTransportType()));
            shipmentPathList.add(shipmentPath);
        }
        shipmentPathMessage.setShipmentPath(shipmentPathList);
        return shipmentPathMessage;
    }

    @Override
    public PackageDimensionsMessage mapShipmentDomainToPackageDimensions(Shipment shipmentDomain) {
        PackageDimensionsMessage packageDimensions = new PackageDimensionsMessage();
        Package shpPackage = shipmentDomain.getShipmentPackage();
        packageDimensions.setPackageId(shpPackage.getId());
        PackageDimension shpPkgDimensions = shpPackage.getDimension();
        packageDimensions.setLength(shpPkgDimensions.getLength());
        packageDimensions.setHeight(shpPkgDimensions.getHeight());
        packageDimensions.setWidth(shpPkgDimensions.getWidth());
        packageDimensions.setMeasurement(shpPkgDimensions.getMeasurementUnit().getLabel());
        packageDimensions.setGrossWeight(shpPkgDimensions.getGrossWeight());
        packageDimensions.setOrgId(shipmentDomain.getOrganization().getId());
        packageDimensions.setCustom(shpPkgDimensions.isCustom());

        return packageDimensions;
    }

    @Override
    public MilestoneMessage mapShipmentDomainToMilestoneMessage(Shipment shipmentDomain) {
        MilestoneMessage milestone = new MilestoneMessage();
        milestone.setPackageId(shipmentDomain.getShipmentPackage().getId());
        milestone.setUserId(shipmentDomain.getUserId());
        milestone.setOrganizationId(shipmentDomain.getOrganization().getId());
        milestone.setActive(true);
        return milestone;
    }

    @Override
    public ShipmentCancelMessage mapShipmentDomainToShipmentCancelMessage(Shipment shipmentDomain) {
        ShipmentCancelMessage shipmentCancel = new ShipmentCancelMessage();
        shipmentCancel.setOrganisationId(shipmentDomain.getOrganization().getId());
        shipmentCancel.setOrderId(shipmentDomain.getOrder().getId());
        shipmentCancel.setShipmentId(shipmentDomain.getId());

        return shipmentCancel;
    }
}
