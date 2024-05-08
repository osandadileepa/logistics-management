package com.quincus.networkmanagement.api.domain;

import com.quincus.networkmanagement.api.validator.constraint.ValidShipmentProfileExtension;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Connection's shipmentProfile class which is an extension of
 * Node's shipmentProfile, and has additional fields
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ValidShipmentProfileExtension
public class ShipmentProfileExtension extends ShipmentProfile {
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxSingleSide;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minSingleSide;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxLinearDim;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minLinearDim;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxVolume;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minVolume;
}
