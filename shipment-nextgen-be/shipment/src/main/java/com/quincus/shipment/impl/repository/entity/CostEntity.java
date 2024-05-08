package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.ProofOfCost;
import com.quincus.shipment.impl.repository.entity.component.CostTypeEntity;
import com.quincus.shipment.impl.repository.entity.component.CurrencyEntity;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "cost")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class CostEntity extends MultiTenantEntity {
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "cost_type_id", length = 48))
    @AttributeOverride(name = "name", column = @Column(name = "cost_type_name", length = 256))
    @AttributeOverride(name = "description", column = @Column(name = "cost_type_description", length = 256))
    @AttributeOverride(name = "category", column = @Column(name = "cost_type_cost_category", length = 64))
    @AttributeOverride(name = "proof", column = @Column(name = "cost_type_proof", length = 16))
    @AttributeOverride(name = "status", column = @Column(name = "cost_type_status", length = 16))
    private CostTypeEntity costType;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "currency_id", length = 48))
    @AttributeOverride(name = "name", column = @Column(name = "currency_name", length = 256))
    @AttributeOverride(name = "code", column = @Column(name = "currency_code", length = 8))
    @AttributeOverride(name = "symbol", column = @Column(name = "currency_symbol", length = 8))
    private CurrencyEntity currency;
    @Column(name = "cost_amount", precision = 15, scale = 2)
    private BigDecimal costAmount;
    @Column(name = "driver_id", length = 48)
    private String driverId;
    @Column(name = "driver_name", length = 256)
    private String driverName;
    @Column(name = "issued_date")
    private LocalDateTime issuedDate;
    @Column(name = "issued_timezone", length = 15)
    private String issuedTimezone;
    @Column(name = "shipments")
    @Type(type = "json")
    private List<CostShipment> shipments;
    @Column(name = "proof_of_cost")
    @Type(type = "json")
    private List<ProofOfCost> proofOfCost;
    @Column(name = "source", length = 50)
    private String source;
    @Column(name = "remarks", length = 2000)
    private String remarks;
    @Column(name = "partner_id", length = 48)
    private String partnerId;
    @Column(name = "partner_name", length = 256)
    private String partnerName;
    @Column(name = "created_by", length = 50)
    private String createdBy;
    @Column(name = "modified_by", length = 50)
    private String modifiedBy;
    @Column(name = "created_timezone", length = 15)
    private String createdTimezone;
    @Column(name = "location_external_ids")
    @Type(type = "json")
    private Set<String> locationExternalIds;
}