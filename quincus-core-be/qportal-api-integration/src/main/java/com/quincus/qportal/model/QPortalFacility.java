package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QPortalFacility {
    @JsonSetter("id")
    private String id;
    @JsonSetter("name")
    private String name;
    @JsonSetter("code")
    private String code;
    @JsonSetter("ancestry_names")
    private String ancestryNames;
    @JsonSetter("facility_type")
    private String facilityType;
    @JsonSetter("location_id")
    private String locationId;
    @JsonSetter("status")
    private String status;
    @JsonSetter("tags")
    private List<String> tags;
}
