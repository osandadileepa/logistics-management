package com.quincus.shipment.impl.repository.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.domain.OrderAttachment;
import com.quincus.shipment.api.domain.OrderReference;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "shipment_order")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class OrderEntity extends MultiTenantEntity {

    @Column(name = "order_id_label", nullable = false, length = 64)
    private String orderIdLabel;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "customer_reference_id")
    @Type(type = "json")
    private List<String> customerReferenceId;

    @Column(name = "order_group", nullable = false, length = 32)
    private String group;

    @Column(name = "pickup_start_time", length = 50)
    private String pickupStartTime;

    @Column(name = "pickup_commit_time", length = 50)
    private String pickupCommitTime;

    @Column(name = "pickup_timezone", length = 48)
    private String pickupTimezone;

    @Column(name = "delivery_start_time", length = 50)
    private String deliveryStartTime;

    @Column(name = "delivery_commit_time", length = 50)
    private String deliveryCommitTime;

    @Column(name = "delivery_timezone", length = 48)
    private String deliveryTimezone;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "tags")
    @Type(type = "json")
    private List<String> tags;

    @Column(name = "attachments")
    @Type(type = "json")
    private List<OrderAttachment> attachments;

    @Column(name = "ops_type", length = 32)
    private String opsType;

    @Column(name = "data")
    @Type(type = "json")
    private JsonNode data;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<InstructionEntity> instructions;

    @Column(name = "order_references")
    @Type(type = "json")
    private List<OrderReference> orderReferences;

    @Override
    public boolean shouldGenerateId() {
        return false;
    }
}
