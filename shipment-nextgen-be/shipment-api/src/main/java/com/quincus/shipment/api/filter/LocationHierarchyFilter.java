package com.quincus.shipment.api.filter;

import com.quincus.shipment.api.constant.LocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class LocationHierarchyFilter extends Filter {
    private String countryId;
    private String stateId;
    private String facilityId;
    private String cityId;
    private LocationType locationType;
    private int level;
}