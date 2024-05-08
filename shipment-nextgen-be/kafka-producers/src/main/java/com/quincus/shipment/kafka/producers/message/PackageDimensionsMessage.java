package com.quincus.shipment.kafka.producers.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PackageDimensionsMessage {
    private String refId; //Package ID from OM Module
    private String packageId; //Package ID from SHP Module
    private BigDecimal length;
    private BigDecimal height;
    private BigDecimal width;
    private String measurement;
    private BigDecimal grossWeight;
    private String orgId;
    @JsonProperty(value = "is_custom")
    private boolean isCustom;
    private String packageTypeId; //If is_custom = true. It means package_type_id is NOT from QPortal
}
