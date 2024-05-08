package com.quincus.shipment.api.domain;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CostFacility {
    @Size(max = 64)
    private String countryName;
    @Size(max = 64)
    private String stateName;
    @Size(max = 64)
    private String cityName;
    @Size(max = 64)
    private String facilityName;
    @Size(max = 48)
    private String countryId;
    @Size(max = 48)
    private String stateId;
    @Size(max = 48)
    private String cityId;
    @Size(max = 48)
    private String facilityId;
}
