package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.networkmanagement.api.deserializer.LatLonSerializer;
import com.quincus.networkmanagement.api.validator.constraint.ValidFacility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ValidFacility
@NoArgsConstructor
@AllArgsConstructor
public class Facility {
    private String id;
    private String name;
    private String code;
    @JsonSerialize(using = LatLonSerializer.class)
    private BigDecimal lat;
    @JsonSerialize(using = LatLonSerializer.class)
    private BigDecimal lon;
    private String timezone;
}
