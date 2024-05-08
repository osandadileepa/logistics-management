package com.quincus.shipment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReference {
    private String id;
    private String orderId;
    private String value;
    private String label;
    private String externalId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
