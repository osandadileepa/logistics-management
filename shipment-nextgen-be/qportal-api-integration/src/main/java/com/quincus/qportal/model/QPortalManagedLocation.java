package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class QPortalManagedLocation {
    @JsonSetter("location_id")
    private String id;
    @JsonSetter("facility_name")
    private String name;
    @JsonSetter("city_state_country")
    private String group;
    @JsonSetter("location_ancestors")
    private QPortalAddress location;
}
