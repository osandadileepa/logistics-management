package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.NetworkLane;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class NetworkLaneFilterResult extends SearchFilterResult<NetworkLane> {
    private long totalElements;
    private long totalPages;
    private long currentPage;
    private NetworkLaneFilter filter;

    public NetworkLaneFilterResult(List<NetworkLane> result) {
        super(result);
    }
}
