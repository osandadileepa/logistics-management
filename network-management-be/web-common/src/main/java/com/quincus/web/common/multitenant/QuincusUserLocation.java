package com.quincus.web.common.multitenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class QuincusUserLocation implements Serializable {
    private String locationId;
    private String locationFacilityName;
    private String locationCode;
}
