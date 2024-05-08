package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quincus.networkmanagement.api.validator.constraint.ValidShipmentProfile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ValidShipmentProfile
public class ShipmentProfile extends Tenant {
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxLength;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minLength;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxWidth;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minWidth;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxHeight;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minHeight;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal maxWeight;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    private BigDecimal minWeight;
    @Override
    @JsonIgnore
    public String getOrganizationId() {
        return super.getOrganizationId();
    }
}
