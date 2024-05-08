package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalFacility extends QPortalModel {
    private String code;
    private Double lat;
    private Double lon;
    private String timezoneTimeInGmt;
    private String locationCode;
}
