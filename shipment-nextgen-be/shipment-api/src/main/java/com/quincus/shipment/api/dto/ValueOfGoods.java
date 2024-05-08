package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.MeasurementUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ValueOfGoods {

    @DecimalMin(value = "0.0")
    private BigDecimal height;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal width;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal length;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal grossWeight;
    private MeasurementUnit measurementUnit;

}
