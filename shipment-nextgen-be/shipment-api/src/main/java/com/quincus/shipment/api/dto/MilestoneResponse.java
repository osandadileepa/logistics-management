package com.quincus.shipment.api.dto;

import lombok.Data;

@Data
public class MilestoneResponse {
    private String id;
    private String name;
    private String code;
    private String status;
    private String mileEquivalent;
}
