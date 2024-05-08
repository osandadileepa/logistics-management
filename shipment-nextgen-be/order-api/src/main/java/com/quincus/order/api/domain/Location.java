package com.quincus.order.api.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Location {
    private String id;
    @NotNull
    private String countryId;
    @NotNull
    private String stateId;
    @NotNull
    private String cityId;
    private String postalCode;
    private String addressLine1;
    private String latitude;
    private String longitude;
    private String addressLine2;
    private String addressLine3;
    private boolean manualCoordinates;
    private String address;
    private String country;
    private String state;
    private String city;
    private String company;
    private String department;
}
