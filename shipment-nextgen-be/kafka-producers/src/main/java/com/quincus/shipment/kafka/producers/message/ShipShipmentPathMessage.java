package com.quincus.shipment.kafka.producers.message;

import lombok.Data;

import java.util.List;

@Data
public class ShipShipmentPathMessage {
    private String id;
    private String organizationId;
    private String orderId;
    private String eta;
    private List<ShipmentPathMessage> shipmentPath;
}
