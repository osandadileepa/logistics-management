package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalLocationCoverage {
    private String id;
    private String name;
    private String countryId;
    private String stateProvinceId;
    private String cityId;
    private LocationType locationType;
    private LocationTag locationTag;
    private String ancestors;
    private String locationCode;

    @Data
    public static class LocationType {
        private String id;
        private String name;
    }

    @Data
    public static class LocationTag {
        private String id;
        private String name;
    }
}