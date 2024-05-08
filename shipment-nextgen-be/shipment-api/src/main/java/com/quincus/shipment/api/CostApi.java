package com.quincus.shipment.api;

import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;

public interface CostApi {

    Cost find(String id);

    Cost create(Cost cost);

    Cost update(Cost cost, String costId);

    CostFilterResult findAllByFilter(CostFilter filter);

    CostShipmentFilterResult findAllCostShipmentByFilter(ShipmentFilter filter);
}
