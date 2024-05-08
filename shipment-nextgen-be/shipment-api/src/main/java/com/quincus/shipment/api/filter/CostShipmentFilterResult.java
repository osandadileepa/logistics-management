package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.CostShipment;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class CostShipmentFilterResult extends SearchFilterResult<CostShipment> {
    private long totalElements;
    private long totalPages;
    private long currentPage;
    private ShipmentFilter filter;

    public CostShipmentFilterResult(List<CostShipment> result) {
        super(result);
    }
}
