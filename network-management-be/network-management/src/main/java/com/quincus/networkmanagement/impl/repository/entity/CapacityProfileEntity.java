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

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "capacity_profile")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class CapacityProfileEntity extends TenantEntity {
    @Column(name = "max_shipment_count")
    private Integer maxShipmentCount;
    @Column(name = "max_weight")
    private BigDecimal maxWeight;
    @Column(name = "max_volume")
    private BigDecimal maxVolume;
}
