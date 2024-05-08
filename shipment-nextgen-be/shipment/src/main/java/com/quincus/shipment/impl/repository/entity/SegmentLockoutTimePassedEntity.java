package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "segment_lockout_time_passed")
public class SegmentLockoutTimePassedEntity extends BaseEntity {

    @Column(name = "segment_id", length = 48)
    private String segmentId;
}
