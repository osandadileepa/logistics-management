package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.now;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "network_lane")
public class NetworkLaneEntity extends MultiTenantEntity {

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "service_type_id", referencedColumnName = "id")
    private ServiceTypeEntity serviceType;

    @Column(name = "service_type_id", insertable = false, updatable = false)
    private String serviceTypeId;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "origin_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity origin;

    @Column(name = "origin_location_hierarchy", insertable = false, updatable = false)
    private String originId;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "destination_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity destination;

    @Column(name = "destination_location_hierarchy", insertable = false, updatable = false)
    private String destinationId;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "networkLane", cascade = CascadeType.ALL)
    private List<NetworkLaneSegmentEntity> networkLaneSegmentList;


    public void addNetworkLaneSegment(NetworkLaneSegmentEntity networkLaneSegment) {
        if (networkLaneSegment == null) return;
        if (this.networkLaneSegmentList == null) {
            this.networkLaneSegmentList = new ArrayList<>();
        }
        networkLaneSegment.setCreateTime(now());
        networkLaneSegmentList.add(networkLaneSegment);
        networkLaneSegment.setNetworkLane(this);
    }

    public void addAllNetworkLaneSegments(List<NetworkLaneSegmentEntity> networkLaneSegments) {
        if (CollectionUtils.isEmpty(networkLaneSegments)) {
            return;
        }
        networkLaneSegments.forEach(this::addNetworkLaneSegment);
    }

    public void resetAndAddAllNetworkLaneSegments(List<NetworkLaneSegmentEntity> networkLaneSegments) {
        if (CollectionUtils.isEmpty(networkLaneSegments)) {
            return;
        }
        if (networkLaneSegmentList != null) {
            for (NetworkLaneSegmentEntity segment : networkLaneSegmentList) {
                segment.setNetworkLane(null);
            }
            networkLaneSegmentList.clear();
        }
        addAllNetworkLaneSegments(networkLaneSegments);
    }

}
