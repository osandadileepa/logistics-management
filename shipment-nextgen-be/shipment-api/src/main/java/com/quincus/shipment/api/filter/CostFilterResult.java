package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.dto.CostSearchResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class CostFilterResult {
    private final List<CostSearchResponse> result;
    private long totalElements;
    private int page;
    private int totalPages;
    private CostFilter filter;

    public CostFilterResult(List<CostSearchResponse> result) {
        this.result = result;
    }

}
