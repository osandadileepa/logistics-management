package com.quincus.qportal.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QPortalPackageType {
    private String id;
    private String name;
    private String description;
    private String organisationId;
    private BigDecimal maxGrossWeight;
    private String maxGrossWeightUnit;
    private BigDecimal maxNetWeight;
    private String maxNetWeightUnit;
    private BigDecimal tareWeight;
    private String tareWeightUnit;
    private BigDecimal internalVolume;
    private String internalVolumeUnit;
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
        private String metric;
    }
}