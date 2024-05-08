package com.quincus.shipment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShipmentResult {
    private final Shipment shipment;
    private final boolean success;
}
