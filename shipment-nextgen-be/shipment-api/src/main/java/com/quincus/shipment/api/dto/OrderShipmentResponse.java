package com.quincus.shipment.api.dto;

import java.util.List;

public record OrderShipmentResponse(String orderId, String orderIdLabel, List<String> shipmentTrackingIds) {
}
