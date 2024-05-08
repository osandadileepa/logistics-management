package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalFacility {
    private String id;
    private String name;
    private String locationTypeId;
    private String locationTagId;
    private String locationCode;
    private String countryId;
    private String stateProvinceId;
    private String cityId;
    private String ancestors;
}
