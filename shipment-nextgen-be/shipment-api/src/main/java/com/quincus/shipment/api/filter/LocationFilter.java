package com.quincus.shipment.api.filter;

import com.quincus.shipment.api.constant.LocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class LocationFilter extends Filter {
    private LocationType type;
}
