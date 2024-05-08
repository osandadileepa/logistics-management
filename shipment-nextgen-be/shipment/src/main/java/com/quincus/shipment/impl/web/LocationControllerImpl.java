package com.quincus.shipment.impl.web;

import com.quincus.shipment.LocationController;
import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LocationControllerImpl implements LocationController {
    private final FilterApi filterApi;

    @Override
    @LogExecutionTime
    public Response<FilterResult> findLocations(final LocationType type,
                                                final int perPage,
                                                final int page,
                                                final String key,
                                                final String sortBy) {
        LocationFilter filter = new LocationFilter();
        filter.setType(type);
        filter.setPage(page);
        filter.setPerPage(perPage);
        filter.setSortBy(sortBy);
        if (StringUtils.isNotEmpty(key)) {
            filter.setKey(key.toLowerCase());
        }
        return new Response<>(filterApi.findLocationsByType(filter));
    }
}
