package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.Shipment;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class ShipmentFilterResult extends SearchFilterResult<Shipment> {
    private long totalElements;
    private long totalPages;
    private long currentPage;
    private ShipmentFilter filter;

    public ShipmentFilterResult(List<Shipment> result) {
        super(result);
    }
}
