package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@Setter
public class PackageJourneyAirSegmentFilterResult {
    private final List<?> result;
    private long totalElements;
    private int page;
    private int totalPages;
    private PackageJourneyAirSegmentFilter filter;

    public PackageJourneyAirSegmentFilterResult(List<?> result) {
        this.result = result;
    }
}