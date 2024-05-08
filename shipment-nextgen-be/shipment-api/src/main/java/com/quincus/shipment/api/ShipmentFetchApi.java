package com.quincus.shipment.api;

import com.quincus.shipment.api.domain.Shipment;

import java.util.List;

public interface ShipmentFetchApi {

    List<Shipment> findAllShipmentsByOrderId(final String orderId);
}
