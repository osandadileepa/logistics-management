package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "commodity")
public class CommodityEntity extends MultiTenantEntity {

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "external_id", length = 48)
    private String externalId;

    @Column(name = "quantity", nullable = false, columnDefinition = "INTEGER UNSIGNED")
    private Long quantity;

    @Column(name = "value", precision = 15, scale = 4)
    private BigDecimal value;

    @Column(name = "description", length = 10000)
    private String description;
    @Column(name = "code", length = 45)
    private String code;
    @Column(name = "hs_code", length = 45)
    private String hsCode;
    @Column(name = "note", length = 10000)
    private String note;
    @Column(name = "packaging_type", length = 45)
    private String packagingType;
}
