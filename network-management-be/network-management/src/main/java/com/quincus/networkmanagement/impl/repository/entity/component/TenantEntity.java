package com.quincus.networkmanagement.impl.repository.entity.component;

import com.quincus.networkmanagement.impl.repository.entity.listener.TenantEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@FilterDef(name = "organizationFilter", parameters = @ParamDef(name = "organizationId", type = "string"))
@Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
@EntityListeners(TenantEntityListener.class)
public abstract class TenantEntity extends BaseEntity {
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
}
