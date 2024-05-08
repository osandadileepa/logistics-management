package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "package")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class PackageEntity extends MultiTenantEntity {
    @Column(name = "ref_id", length = 48)
    private String refId;

    @Column(name = "total_value", precision = 16, scale = 4)
    private BigDecimal totalValue;

    @Column(name = "currency", length = 4)
    private String currency;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @Column(name = "type_reference_id", length = 48)
    private String typeRefId;

    @Column(name = "value", length = 45)
    private String value;

    @Column(name = "ready_time")
    private LocalDateTime readyTime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dimensions_id", referencedColumnName = "id")
    private PackageDimensionEntity dimension;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(joinColumns = @JoinColumn(name = "package_id"), inverseJoinColumns = @JoinColumn(name = "commodities_id"))
    private List<CommodityEntity> commodities;

    @Column(name = "pricing_info")
    @Type(type = "json")
    private PricingInfo pricingInfo;

    @Column(name = "code", length = 45)
    private String code;

    @Column(name = "total_items_count", columnDefinition = "INTEGER UNSIGNED")
    private Long totalItemsCount;

    @Column(name = "source", length = 5)
    @Enumerated(EnumType.STRING)
    private TriggeredFrom source;
}