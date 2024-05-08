package com.quincus.shipment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackageDimensionErrorRecord {
    private String shipmentTrackingId;
    private String packagingType;
    private String unit;
    private String height;
    private String width;
    private String length;
    private String weight;
    private String failedReason;
}
