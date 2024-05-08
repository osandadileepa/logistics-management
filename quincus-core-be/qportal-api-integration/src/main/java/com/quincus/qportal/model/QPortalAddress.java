package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
public class QPortalAddress {
    private static final String DELIMITER = ",";
    @JsonSetter("location_id")
    private String id;
    private String countryId;
    @JsonSetter("state_province_id")
    private String stateId;
    private String cityId;
    @JsonSetter("ancestors")
    private String locationName;
    private String countryName;
    private String stateName;
    private String cityName;

    public String getCountryName() {
        return getParsedName(0);
    }

    public String getStateName() {
        return getParsedName(1);
    }

    public String getCityName() {
        return getParsedName(2);
    }

    private String getParsedName(int index) {
        if (StringUtils.isBlank(locationName) || StringUtils.countMatches(locationName, DELIMITER) != 2 || index < 0 || index > 2) {
            return locationName;
        }
        return Arrays.stream(StringUtils.split(locationName, DELIMITER)).toList().get(index);
    }
}
