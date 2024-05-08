package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "service_type")
public class ServiceTypeEntity extends MultiTenantEntity {

    @Column(name = "code", length = 64)
    private String code;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "description", length = 256)
    private String description;
}
