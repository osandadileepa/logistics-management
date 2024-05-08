package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QPortalLocation extends QPortalModel {
    private String countryId;
    private String stateProvinceId;
    private String cityId;
    private String code;
    private boolean active;
    private String zipcode;
    @JsonProperty("address_1")
    private String address1;
    @JsonProperty("address_2")
    private String address2;
    @JsonProperty("address_3")
    private String address3;
    private String locationType;
    private String ancestors;
    private String timezoneTimeInGmt;
    private String locationCode;
    private String lat;
    private String lon;
}
