package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import com.quincus.networkmanagement.impl.repository.entity.embeddable.CurrencyEmbeddable;
import com.quincus.networkmanagement.impl.repository.entity.embeddable.NodeEmbeddable;
import com.quincus.networkmanagement.impl.repository.entity.embeddable.VehicleTypeEmbeddable;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "connection")
@Where(clause = "deleted = false")
@Filter(name = "deletedFilter")
@FilterDef(name = "deletedFilter", defaultCondition = "isDeleted = false")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class ConnectionEntity extends TenantEntity {
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "connection_code")
    private String connectionCode;
    @Column(name = "active")
    private boolean active = true;
    @ElementCollection
    @CollectionTable(
            name = "connection_tag",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    @Column(name = "tag")
    private List<String> tags;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "partner_id"))
    @AttributeOverride(name = "code", column = @Column(name = "partner_code"))
    @AttributeOverride(name = "name", column = @Column(name = "partner_name"))
    private Partner vendor;
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportType transportType;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "vehicle_type_id"))
    @AttributeOverride(name = "name", column = @Column(name = "vehicle_type_name"))
    private VehicleTypeEmbeddable vehicleType;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "departure_node_id"))
    @AttributeOverride(name = "nodeCode", column = @Column(name = "departure_node_code"))
    private NodeEmbeddable departureNode;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "arrival_node_id"))
    @AttributeOverride(name = "nodeCode", column = @Column(name = "arrival_node_code"))
    private NodeEmbeddable arrivalNode;
    @Column(name = "cost")
    private BigDecimal cost;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "currency_id"))
    @AttributeOverride(name = "code", column = @Column(name = "currency_code"))
    @AttributeOverride(name = "name", column = @Column(name = "currency_name"))
    @AttributeOverride(name = "exchangeRate", column = @Column(name = "currency_exchange_rate"))
    private CurrencyEmbeddable currency;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "shipment_profile_id", referencedColumnName = "id")
    @ToString.Exclude
    private ShipmentProfileEntity shipmentProfile;
    @Column(name = "duration")
    private Integer duration;
    @Column(name = "air_lockout_duration")
    private Integer airLockoutDuration;
    @Column(name = "air_recovery_duration")
    private Integer airRecoveryDuration;
    @Column(name = "distance")
    private BigDecimal distance;
    @ElementCollection
    @CollectionTable(
            name = "connection_schedule",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    @Column(name = "schedule")
    private List<String> schedules;
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "capacity_profile_id", referencedColumnName = "id")
    private CapacityProfileEntity capacityProfile;
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "measurement_units_id", referencedColumnName = "id")
    private MeasurementUnitsEntity measurementUnits;
}
