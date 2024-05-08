package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class PackageJourneyAirSegmentResponse {
    private String id;
    private String name;
    private String code;
    @JsonProperty(value = "children")
    private List<FlightDetailsResponse> flightNumbers;
}
