package com.quincus.shipment.impl.web;

import com.quincus.shipment.NetworkLaneController;
import com.quincus.shipment.api.NetworkLaneApi;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class NetworkLaneControllerImpl implements NetworkLaneController {
    private final NetworkLaneApi networkLaneApi;

    @Override
    @LogExecutionTime
    public Response<NetworkLaneFilterResult> findAll(Request<NetworkLaneFilter> requestFilter) {
        return new Response<>(networkLaneApi.findAll(requestFilter.getData()));
    }

    @Override
    @LogExecutionTime
    public Response<NetworkLane> update(Request<NetworkLane> request) {
        final NetworkLane networkLane = request.getData();
        return new Response<>(networkLaneApi.update(networkLane));
    }

    @Override
    @LogExecutionTime
    public Response<NetworkLane> findById(String id) {
        return new Response<>(networkLaneApi.findById(id));
    }
}
