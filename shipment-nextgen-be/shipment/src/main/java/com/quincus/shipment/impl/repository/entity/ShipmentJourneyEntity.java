package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.Instant.now;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = "shipment_journey")
public class ShipmentJourneyEntity extends MultiTenantEntity {
    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private JourneyStatus status;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "shipmentJourney", cascade = CascadeType.ALL)
    private List<PackageJourneySegmentEntity> packageJourneySegments;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipment_journey_id")
    private List<AlertEntity> alerts;

    public void addPackageJourneySegment(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegmentEntity != null) {
            if (this.packageJourneySegments == null) {
                this.packageJourneySegments = new ArrayList<>();
            }
            packageJourneySegmentEntity.setCreateTime(now(Clock.systemUTC()));
            packageJourneySegmentEntity.setDeleted(false);
            packageJourneySegments.add(packageJourneySegmentEntity);
            packageJourneySegmentEntity.setShipmentJourney(this);
        }
    }

    public void addAllPackageJourneySegments(List<PackageJourneySegmentEntity> packageJourneySegmentEntityList) {
        if (CollectionUtils.isEmpty(packageJourneySegmentEntityList)) {
            this.packageJourneySegments = Collections.emptyList();
        }
        packageJourneySegmentEntityList.forEach(this::addPackageJourneySegment);
    }

    private PackageJourneySegmentEntity removePackageJourneySegment(PackageJourneySegmentEntity packageJourneySegmentEntity,
                                                                    List<PackageJourneySegmentEntity> removedSegments) {
        if (this.packageJourneySegments == null || packageJourneySegmentEntity == null) return null;
        packageJourneySegments.remove(packageJourneySegmentEntity);
        packageJourneySegmentEntity.setDeleted(true);
        packageJourneySegmentEntity.setModifyTime(Instant.now(Clock.systemUTC()));
        removedSegments.add(packageJourneySegmentEntity);
        return packageJourneySegmentEntity;
    }

    public List<PackageJourneySegmentEntity> removeAllPackageJourneySegments() {
        List<PackageJourneySegmentEntity> removedSegments = new ArrayList<>();
        if (!CollectionUtils.isEmpty(this.packageJourneySegments)) {
            List<PackageJourneySegmentEntity> segmentList = new ArrayList<>(this.packageJourneySegments);
            segmentList.forEach(s -> removePackageJourneySegment(s, removedSegments));
        }
        return removedSegments;
    }
}