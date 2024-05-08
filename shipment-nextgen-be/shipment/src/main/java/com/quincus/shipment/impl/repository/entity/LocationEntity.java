package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "location")
public class LocationEntity extends MultiTenantEntity {
    @Column(name = "type", length = 32)
    @Enumerated(EnumType.STRING)
    private LocationType type;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 64)
    private String description;

    @Column(name = "ext_id", nullable = false, length = 48)
    private String externalId;

    @Column(name = "timezone")
    private String timezone;

}
