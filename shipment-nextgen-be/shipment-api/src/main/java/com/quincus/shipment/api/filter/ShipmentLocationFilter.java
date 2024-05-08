package com.quincus.shipment.api.filter;

import com.quincus.shipment.api.validator.constraint.ValidStringArray;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentLocationFilter {
    @ValidStringArray(uuid = true, message = "must be a valid UUIDv4 format", notNullEach = true)
    private String[] stateIds;
    @ValidStringArray(uuid = true, message = "must be a valid UUIDv4 format", notNullEach = true)
    private String[] countryIds;
    @ValidStringArray(uuid = true, message = "must be a valid UUIDv4 format", notNullEach = true)
    private String[] facilityIds;
    @ValidStringArray(uuid = true, message = "must be a valid UUIDv4 format", notNullEach = true)
    private String[] cityIds;
}
