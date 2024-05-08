package com.quincus.shipment.api.dto.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkLaneCsv {

    @JsonIgnore
    private long row;
    private String laneId;
    private String serviceType;
    private String originLocationTreeLevel1;
    private String originLocationTreeLevel2;
    private String originLocationTreeLevel3;
    private String originLocationTreeLevel4;
    private String originLocationTreeLevel5;
    private String originFacilityId;
    private String destinationLocationTreeLevel1;
    private String destinationLocationTreeLevel2;
    private String destinationLocationTreeLevel3;
    private String destinationLocationTreeLevel4;
    private String destinationLocationTreeLevel5;
    private String destinationFacilityId;
    @JsonIgnore
    private String organizationId;
    @Valid
    private List<NetworkLaneSegmentCsv> networkLaneSegments;

    private String failedReason;

    public void addAllNetworkLaneSegmentCsv(List<NetworkLaneSegmentCsv> networkLaneSegmentCsvList){
        if (this.networkLaneSegments == null) {
            this.networkLaneSegments = new ArrayList<>();
        }
        this.networkLaneSegments.addAll(networkLaneSegmentCsvList);
    }

}
