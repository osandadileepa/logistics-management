package com.quincus.shipment.impl.web;

import com.quincus.shipment.CostController;
import com.quincus.shipment.api.CostApi;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class CostControllerImpl implements CostController {

    private final CostApi costApi;

    @Override
    @PreAuthorize("hasAuthority('COST_VIEW')")
    @LogExecutionTime
    public Response<Cost> find(final String id) {
        return new Response<>(this.costApi.find(id));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('COST_CREATE','S2S')")
    @LogExecutionTime
    public Response<Cost> add(final Request<Cost> request) {
        final Cost cost = request.getData();
        return new Response<>(costApi.create(cost));
    }

    @Override
    @PreAuthorize("hasAuthority('COST_EDIT')")
    @LogExecutionTime
    public Response<Cost> update(final Request<Cost> request, final String id) {
        final Cost cost = request.getData();
        return new Response<>(costApi.update(cost, id));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('COST_CREATE', 'COST_EDIT')")
    @LogExecutionTime
    public Response<CostShipmentFilterResult> findAllCostShipmentByFilter(final Request<ShipmentFilter> shipmentFilter) {
        return new Response<>(costApi.findAllCostShipmentByFilter(shipmentFilter.getData()));
    }

    @Override
    @PreAuthorize("hasAuthority('COST_VIEW')")
    @LogExecutionTime
    public Response<CostFilterResult> findAllByFilter(final Request<CostFilter> costFilter) {
        return new Response<>(costApi.findAllByFilter(costFilter.getData()));
    }
}
