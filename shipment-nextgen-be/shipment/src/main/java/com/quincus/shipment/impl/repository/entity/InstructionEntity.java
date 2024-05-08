package com.quincus.shipment.impl.repository.entity;


import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "instruction")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class InstructionEntity extends MultiTenantEntity {
    @Column(name = "external_id")
    private String externalId;

    @Column(name = "label")
    private String label;

    @Column(name = "source")
    private String source;

    @Column(name = "value", length = 4000)
    private String value;

    @Column(name = "apply_to")
    @Enumerated(EnumType.STRING)
    private InstructionApplyToType applyTo;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "order_id", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String orderId;

    @Column(name = "package_journey_segment_id", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String packageJourneySegmentId;
}
