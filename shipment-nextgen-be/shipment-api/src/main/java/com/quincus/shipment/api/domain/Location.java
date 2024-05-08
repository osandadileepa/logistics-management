package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.LocationType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@Getter
@Setter
public class Location {
    @UUID(required = false)
    private String id;
    @UUID(required = false)
    private String organizationId;
    @Valid
    private LocationType type;
    @Size(max = 64)
    private String country;
    @Size(max = 64)
    private String state;
    @Size(max = 64)
    private String city;
    @Size(max = 255)
    private String facilityName;
    @Size(max = 15)
    private String timezone;
}
