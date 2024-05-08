package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CapacityProfile extends Tenant {
    @Min(0)
    @Max(999999)
    @NotNull
    private Integer maxShipmentCount;
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    @NotNull
    private BigDecimal maxWeight;
    @DecimalMin("0.0")
    @DecimalMax("999999.0")
    @NotNull
    private BigDecimal maxVolume;
    @Override
    @JsonIgnore
    public String getOrganizationId() {
        return super.getOrganizationId();
    }
}
