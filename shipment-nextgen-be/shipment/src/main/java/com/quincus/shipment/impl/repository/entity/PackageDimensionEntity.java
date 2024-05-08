package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "package_dimension")
public class PackageDimensionEntity extends MultiTenantEntity {
    @Column(name = "measurement_unit", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private MeasurementUnit measurementUnit;

    @Column(name = "volume_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal volumeWeight;

    @Column(name = "gross_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "chargeable_weight", nullable = false, precision = 15, scale = 3)
    private BigDecimal chargeableWeight;

    @Column(name = "length", nullable = false, precision = 15, scale = 3)
    private BigDecimal length;

    @Column(name = "width", nullable = false, precision = 15, scale = 3)
    private BigDecimal width;

    @Column(name = "height", nullable = false, precision = 15, scale = 3)
    private BigDecimal height;

    @Column(name = "custom")
    private boolean custom;
}
