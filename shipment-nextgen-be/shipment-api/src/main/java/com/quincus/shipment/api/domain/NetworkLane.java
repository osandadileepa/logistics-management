package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NetworkLane {

    @UUID(required = false)
    private String id;
    @Valid
    private ServiceType serviceType;
    @Valid
    private Address origin;
    @Valid
    private Address destination;
    @Valid
    private Facility originFacility;
    @Valid
    private Facility destinationFacility;
    @UUID(required = false)
    private String organizationId;

    @Size(min = 1)
    private List<@Valid NetworkLaneSegment> networkLaneSegments;

    @JsonIgnore
    public List<NetworkLaneSegment> getUnsortedNetworkLaneSegments() {
        return networkLaneSegments;
    }

    public List<NetworkLaneSegment> getNetworkLaneSegments() {
        if (CollectionUtils.isEmpty(networkLaneSegments)) return Collections.emptyList();
        List<NetworkLaneSegment> sortedSegments = new ArrayList<>(networkLaneSegments);
        sortedSegments.sort(Comparator.comparing(NetworkLaneSegment::getSequence));
        return sortedSegments;
    }

    public void addNetworkLaneSegment(NetworkLaneSegment segment) {
        if (CollectionUtils.isEmpty(networkLaneSegments)) {
            networkLaneSegments = new ArrayList<>();
        }
        networkLaneSegments.add(segment);
    }

}
