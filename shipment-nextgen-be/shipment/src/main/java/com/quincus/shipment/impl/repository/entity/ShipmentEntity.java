package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.springframework.util.CollectionUtils;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "shipment")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@AttributeOverride(name = MultiTenantEntity_.ORGANIZATION_ID,
        column = @Column(name = "organization_id", insertable = false, updatable = false))
@FilterDef(name = "deletedFilter", defaultCondition = "isDeleted = false")
@Filter(name = "deletedFilter")
@Where(clause = "deleted=false")
public class ShipmentEntity extends MultiTenantEntity {
    @Column(name = "shipment_tracking_id", nullable = false, length = 48)
    private String shipmentTrackingId;

    @Column(name = "partner_id", nullable = false, length = 48)
    private String partnerId;

    @Column(name = "pick_up_location", nullable = false, length = 256)
    private String pickUpLocation;

    @Column(name = "delivery_location", nullable = false, length = 256)
    private String deliveryLocation;

    @Column(name = "return_location", length = 256)
    private String returnLocation;

    @Column(name = "tag", length = 128)
    private String tag;

    @Column(name = "extra_care_info")
    @Type(type = "json")
    private List<String> extraCareInfo;

    @Column(name = "insurance_info")
    @Type(type = "json")
    private List<String> insuranceInfo;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    private ServiceTypeEntity serviceType;

    @Column(name = "user_id", nullable = false, length = 48)
    private String userId;

    @Column(name = "sender")
    @Type(type = "json")
    private Sender sender;

    @Column(name = "consignee")
    @Type(type = "json")
    private Consignee consignee;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "origin", nullable = false, referencedColumnName = "id")
    private AddressEntity origin;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "destination", nullable = false, referencedColumnName = "id")
    private AddressEntity destination;

    @Column(name = "origin", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String originId;

    @Column(name = "destination", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String destinationId;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "package_id", nullable = false, referencedColumnName = "id")
    private PackageEntity shipmentPackage;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "shipment_journey_id", nullable = false, referencedColumnName = "id")
    private ShipmentJourneyEntity shipmentJourney;

    @Column(name = "shipment_journey_id", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String shipmentJourneyId;

    @OneToOne(optional = false, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
    private OrganizationEntity organization;

    @ManyToOne(optional = false, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "order_id", nullable = false, referencedColumnName = "id")
    private OrderEntity order;

    @Column(name = "order_id", insertable = false, updatable = false, length = 48)
    @Setter(AccessLevel.NONE)
    private String orderId;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customer;

    @Column(name = "status", length = 32)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Column(name = "eta_status", length = 8)
    @Enumerated(EnumType.STRING)
    private EtaStatus etaStatus;

    @Column(name = "instructions")
    @Type(type = "json")
    private List<Instruction> instructions;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "shipment_reference_id")
    @Type(type = "json")
    private List<String> shipmentReferenceId;

    @Column(name = "shipment_tags")
    @Type(type = "json")
    private List<String> shipmentTags;

    @Column(name = "shipment_attachments")
    @Type(type = "json")
    private List<HostedFile> shipmentAttachments;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shipment")
    private Set<MilestoneEntity> milestoneEvents;
    @Column(name = "internal_order_id", length = 64)
    private String internalOrderId;
    @Column(name = "external_order_id", length = 64)
    private String externalOrderId;
    @Column(name = "customer_order_id", length = 64)
    private String customerOrderId;
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "description")
    private String description;

    @Column(name = "distance_uom", length = 10)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure distanceUom;

    public Set<MilestoneEntity> getMilestoneEvents() {
        if (CollectionUtils.isEmpty(milestoneEvents)) return Collections.emptySet();
        TreeSet<MilestoneEntity> sortedSet = new TreeSet<>(Collections.reverseOrder());
        sortedSet.addAll(milestoneEvents);
        return sortedSet;
    }
}
