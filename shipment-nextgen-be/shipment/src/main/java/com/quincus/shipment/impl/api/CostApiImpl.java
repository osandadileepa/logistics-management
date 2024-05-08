package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.CostApi;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.service.CostService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CostApiImpl implements CostApi {

    private final CostService costService;

    @Override
    public Cost find(String id) {
        return costService.find(id);
    }

    @Override
    public Cost create(Cost cost) {
        return costService.create(cost);
    }

    @Override
    public Cost update(Cost cost, String costId) {
        return costService.update(cost, costId);
    }

    @Override
    public CostFilterResult findAllByFilter(CostFilter filter) {
        return costService.findAllByFilter(filter);
    }

    @Override
    public CostShipmentFilterResult findAllCostShipmentByFilter(ShipmentFilter filter) {
        return costService.findAllCostShipmentByFilter(filter);
    }

}
