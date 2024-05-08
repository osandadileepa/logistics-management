package com.quincus.core.impl.repository.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public class TenantEntity extends BaseEntity {
    @Column(name = "organization_id")
    private String organizationId;
}