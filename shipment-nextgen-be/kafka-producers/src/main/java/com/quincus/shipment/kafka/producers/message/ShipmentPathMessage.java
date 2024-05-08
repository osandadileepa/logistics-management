package com.quincus.shipment.kafka.producers.message;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipmentPathMessage {
    private String id;
    private String hubId;
    private String partnerId;
    private String transportId;
    private String transportType;
    private String transportCategory;
    private Integer position;
    private String currencyCode;
    private BigDecimal cost;
    private String openedAt;
    private String closedAt;
}
