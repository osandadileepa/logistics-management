package com.quincus.shipment.kafka.producers.message;

import lombok.Data;

@Data
public class ShipmentCancelMessage {
    private String shipmentId;
    private String orderId;
    private String organisationId;
}

