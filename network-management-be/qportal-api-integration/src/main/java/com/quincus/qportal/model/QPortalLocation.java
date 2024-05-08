package com.quincus.qportal.model;

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
    private String address1;
    private String address2;
    private String address3;
    private String locationType;
    private String ancestors;
    private String timezoneTimeInGmt;
    private String locationCode;
}
