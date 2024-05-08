package com.quincus.shipment.api.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
public class NetworkLaneFilter {

    private List<@Valid ServiceTypeFilter> serviceTypes;
    @Valid
    private NetworkLaneLocationFilter originLocations;
    @Valid
    private NetworkLaneLocationFilter destinationLocations;
    @Valid
    private NetworkLaneLocationFilter facilityLocations;
    @Min(1)
    @Max(100)
    private int size;
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int pageNumber;

    public int getPageNumber() {
        return this.pageNumber - 1;
    }
}