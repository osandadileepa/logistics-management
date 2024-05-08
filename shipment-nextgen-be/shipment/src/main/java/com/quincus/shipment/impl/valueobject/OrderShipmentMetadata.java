package com.quincus.shipment.impl.valueobject;

import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;

public record OrderShipmentMetadata(OrganizationEntity organization,
                                    OrderEntity order,
                                    CustomerEntity customer,
                                    AddressEntity origin,
                                    AddressEntity destination,
                                    ServiceTypeEntity serviceType,
                                    ShipmentJourneyEntity shipmentJourney) {
}
