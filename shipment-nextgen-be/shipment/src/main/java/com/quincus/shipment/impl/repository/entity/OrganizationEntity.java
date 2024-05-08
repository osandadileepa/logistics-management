package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
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
@Table(name = "organization")
public class OrganizationEntity extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "code", length = 32)
    private String code;

    @Override
    public boolean shouldGenerateId() {
        return false;
    }
}
