package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

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
@Table(name = "package_log")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class PackageLogEntity extends MultiTenantEntity {

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "package_id")
    private String packageId;

    @Column(name = "source", length = 5)
    @Enumerated(EnumType.STRING)
    private TriggeredFrom source;

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

    @Column(name = "is_custom")
    private boolean custom;
}