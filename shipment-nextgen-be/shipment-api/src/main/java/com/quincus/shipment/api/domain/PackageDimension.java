package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.MeasurementUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PackageDimension {
    @UUID(required = false)
    private String id;
    @NotNull
    private MeasurementUnit measurementUnit;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal length;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal width;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal height;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal grossWeight;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal volumeWeight;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(fraction = 3, integer = 15)
    private BigDecimal chargeableWeight;
    private boolean custom;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
}
