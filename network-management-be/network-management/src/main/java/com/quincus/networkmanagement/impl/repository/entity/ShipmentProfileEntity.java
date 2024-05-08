package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "shipment_profile")
public class ShipmentProfileEntity extends TenantEntity {
    @Column(name = "max_length")
    private BigDecimal maxLength;
    @Column(name = "min_length")
    private BigDecimal minLength;
    @Column(name = "max_width")
    private BigDecimal maxWidth;
    @Column(name = "min_width")
    private BigDecimal minWidth;
    @Column(name = "max_height")
    private BigDecimal maxHeight;
    @Column(name = "min_height")
    private BigDecimal minHeight;
    @Column(name = "max_weight")
    private BigDecimal maxWeight;
    @Column(name = "min_weight")
    private BigDecimal minWeight;
    @Column(name = "max_single_side")
    private BigDecimal maxSingleSide;
    @Column(name = "min_single_side")
    private BigDecimal minSingleSide;
    @Column(name = "max_linear_dim")
    private BigDecimal maxLinearDim;
    @Column(name = "min_linear_dim")
    private BigDecimal minLinearDim;
    @Column(name = "max_volume")
    private BigDecimal maxVolume;
    @Column(name = "min_volume")
    private BigDecimal minVolume;
}
