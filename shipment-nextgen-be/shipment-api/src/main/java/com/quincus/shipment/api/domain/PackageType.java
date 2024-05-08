package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.PackageTypeMetricUnit;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PackageType {
    private String id;
    private String name;
    private String description;
    private String organizationId;
    private BigDecimal maxGrossWeight;
    private PackageTypeMetricUnit maxGrossWeightUnit;
    private BigDecimal maxNetWeight;
    private PackageTypeMetricUnit maxNetWeightUnit;
    private BigDecimal tareWeight;
    private PackageTypeMetricUnit tareWeightUnit;
    private BigDecimal internalVolume;
    private PackageTypeMetricUnit internalVolumeUnit;
    private String deletedAt;
    private boolean orderPackaging;
    private boolean shipmentPackaging;
    private Dimension doorAperture;
    private Dimension internalDimension;
    private Dimension dimension;

    @Data
    public static class Dimension {
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private PackageTypeMetricUnit metric;
    }
}
