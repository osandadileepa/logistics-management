package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import com.quincus.shipment.kafka.producers.message.ShipShipmentPathMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentCancelMessage;

public interface ShipmentToOrderMapper {

    ShipShipmentPathMessage mapShipmentDomainToShipmentPathMessage(Shipment shipmentDomain);

    PackageDimensionsMessage mapShipmentDomainToPackageDimensions(Shipment shipmentDomain);

    MilestoneMessage mapShipmentDomainToMilestoneMessage(Shipment shipmentDomain);

    ShipmentCancelMessage mapShipmentDomainToShipmentCancelMessage(Shipment shipmentDomain);
}
