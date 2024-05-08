package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.Shipment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class ShipmentExportFilterResult extends SearchFilterResult<Shipment> {
    private ExportFilter filter;

    @Builder
    public ShipmentExportFilterResult(List<Shipment> result) {
        super(result);
    }

}
