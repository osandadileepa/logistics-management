package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "measurement_units")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class MeasurementUnitsEntity extends TenantEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    private WeightUnit weightUnit;
    @Enumerated(EnumType.STRING)
    @Column(name = "volume_unit")
    private VolumeUnit volumeUnit;
    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_unit")
    private DimensionUnit dimensionUnit;
    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit")
    private DistanceUnit distanceUnit;
}
