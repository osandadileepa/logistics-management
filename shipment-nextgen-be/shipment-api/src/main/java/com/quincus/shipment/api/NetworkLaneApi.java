package com.quincus.shipment.api;

import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;

public interface NetworkLaneApi {
    NetworkLaneFilterResult findAll(NetworkLaneFilter filter);

    NetworkLane update(NetworkLane networkLane);

    NetworkLane findById(String id);
}
