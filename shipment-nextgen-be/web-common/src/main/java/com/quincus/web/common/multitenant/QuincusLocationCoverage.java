package com.quincus.web.common.multitenant;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class QuincusLocationCoverage implements Serializable {
    public static final String LOCATION_TYPE_COUNTRY = "COUNTRY";
    public static final String LOCATION_TYPE_STATE = "STATE/PROVINCE";
    public static final String LOCATION_TYPE_CITY = "CITY";
    public static final String LOCATION_TYPE_FACILITY = "FACILITY";

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
    public static class LocationType implements Serializable {
        private String id;
        private String name;
    }

    @Data
    public static class LocationTag implements Serializable {
        private String id;
        private String name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuincusLocationCoverage that)) {
            return false;
        }
        return (id != null) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}