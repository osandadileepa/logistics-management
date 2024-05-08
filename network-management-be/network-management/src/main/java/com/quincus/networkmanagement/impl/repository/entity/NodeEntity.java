package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import com.quincus.networkmanagement.impl.repository.entity.embeddable.FacilityEmbeddable;
import com.quincus.networkmanagement.impl.repository.entity.embeddable.PartnerEmbeddable;
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
import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "node")
@Where(clause = "deleted = false")
@Filter(name = "deletedFilter")
@FilterDef(name = "deletedFilter", defaultCondition = "isDeleted = false")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class NodeEntity extends TenantEntity {
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "node_type")
    @Enumerated(EnumType.STRING)
    private NodeType nodeType;
    @Column(name = "node_code", unique = true)
    private String nodeCode;
    @Column(name = "description")
    private String description;
    @Column(name = "active")
    private boolean active = true;
    @ElementCollection
    @CollectionTable(
            name = "node_tag",
            joinColumns = @JoinColumn(name = "node_id")
    )
    @Column(name = "tag")
    private List<String> tags;
    @Column(name = "address_line_1")
    private String addressLine1;
    @Column(name = "address_line_2")
    private String addressLine2;
    @Column(name = "address_line_3")
    private String addressLine3;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "facility_id"))
    @AttributeOverride(name = "code", column = @Column(name = "facility_code"))
    @AttributeOverride(name = "name", column = @Column(name = "facility_name"))
    @AttributeOverride(name = "lat", column = @Column(name = "facility_lat"))
    @AttributeOverride(name = "lon", column = @Column(name = "facility_lon"))
    private FacilityEmbeddable facility;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "partner_id"))
    @AttributeOverride(name = "code", column = @Column(name = "partner_code"))
    @AttributeOverride(name = "name", column = @Column(name = "partner_name"))
    private PartnerEmbeddable vendor;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "operating_hours_id", referencedColumnName = "id")
    @ToString.Exclude
    private OperatingHoursEntity operatingHours;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "shipment_profile_id", referencedColumnName = "id")
    @ToString.Exclude
    private ShipmentProfileEntity shipmentProfile;
    @Column(name = "timezone")
    private String timezone;
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "capacity_profile_id", referencedColumnName = "id")
    private CapacityProfileEntity capacityProfile;
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "measurement_units_id", referencedColumnName = "id")
    private MeasurementUnitsEntity measurementUnits;
}
