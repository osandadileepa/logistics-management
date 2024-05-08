package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MeasurementUnits extends Tenant {
    @NotNull
    private WeightUnit weightUnit;
    @NotNull
    private VolumeUnit volumeUnit;
    @NotNull
    private DimensionUnit dimensionUnit;
    private DistanceUnit distanceUnit;
    @Override
    @JsonIgnore
    public String getOrganizationId() {
        return super.getOrganizationId();
    }
}
