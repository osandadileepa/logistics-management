package com.quincus.shipment.api.filter;

import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NetworkLaneLocationFilter {
    private List<@UUID String> stateIds;
    private List<@UUID String> stateExtIds;
    private List<@UUID String> countryIds;
    private List<@UUID String> countryExtIds;
    private List<@UUID String> facilityIds;
    private List<@UUID String> facilityExtIds;
    private List<@UUID String> cityIds;
    private List<@UUID String> cityExtIds;
}
