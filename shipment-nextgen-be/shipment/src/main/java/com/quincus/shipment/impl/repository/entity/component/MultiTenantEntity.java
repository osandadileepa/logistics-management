package com.quincus.shipment.impl.repository.entity.component;

import com.quincus.shipment.impl.repository.listeners.MultiTenantEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@EntityListeners(MultiTenantEntityListener.class)
public class MultiTenantEntity extends BaseEntity {
    @Column(name = "organization_id", length = 48)
    private String organizationId;
}
