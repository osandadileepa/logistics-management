package com.quincus.shipment.impl.repository.criteria;

import com.quincus.shipment.api.constant.LocationType;

import java.util.List;
import java.util.Map;

public interface LocationCoverageCriteria {

    void setUserLocationCoverageIdsByType(Map<LocationType, List<String>> userLocationCoverageByType);
}
