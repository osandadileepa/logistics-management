package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;

public interface ShipmentToPackageDimensionsMapper {
    PackageDimensionsMessage mapShipmentToPackageDimensionsMessage(Shipment shipment);
}
