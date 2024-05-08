package com.quincus.shipment.impl.web;

import com.quincus.shipment.LocationHierarchyController;
import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LocationHierarchyControllerImpl implements LocationHierarchyController {
    private final FilterApi filterApi;

    @Override
    @LogExecutionTime
    public Response<FilterResult> findLocationHierarchies(final String countryId,
                                                          final String stateId,
                                                          final String cityId,
                                                          final String facilityId,
                                                          final int page,
                                                          final int per_page,
                                                          final String key,
                                                          final int level) {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId(countryId);
        filter.setStateId(stateId);
        filter.setCityId(cityId);
        filter.setFacilityId(facilityId);
        filter.setPage(page);
        filter.setPerPage(per_page);
        filter.setLevel(level);
        if (StringUtils.isNotEmpty(key)) {
            filter.setKey(key.toLowerCase());
        }
        return new Response<>(filterApi.findLocationHierarchies(filter));
    }

    @Override
    @LogExecutionTime
    public Response<FilterResult> findAllStatesByCountry(final String country_id,
                                                         final int perPage,
                                                         final int page,
                                                         final String sortBy) {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setCountryId(country_id);
        filter.setPage(page);
        filter.setPerPage(perPage);
        filter.setSortBy(sortBy);
        filter.setLocationType(LocationType.STATE);
        return new Response<>(filterApi.findAllStatesByCountry(filter));
    }

    @Override
    @LogExecutionTime
    public Response<FilterResult> findAllCitiesByStateCountry(final String countryId,
                                                              final String stateId,
                                                              final int perPage,
                                                              final int page,
                                                              final String sortBy) {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setStateId(stateId);
        filter.setCountryId(countryId);
        filter.setPage(page);
        filter.setPerPage(perPage);
        filter.setSortBy(sortBy);
        filter.setLocationType(LocationType.CITY);
        return new Response<>(filterApi.findAllCitiesByStateCountry(filter));
    }

    @Override
    @LogExecutionTime
    public Response<FilterResult> findAllFacilitiesByCityStateCountry(final String countryId,
                                                                      final String stateId,
                                                                      final String cityId,
                                                                      final int perPage,
                                                                      final int page,
                                                                      final String sortBy) {
        LocationHierarchyFilter filter = new LocationHierarchyFilter();
        filter.setStateId(stateId);
        filter.setCountryId(countryId);
        filter.setCityId(cityId);
        filter.setPage(page);
        filter.setPerPage(perPage);
        filter.setSortBy(sortBy);
        filter.setLocationType(LocationType.FACILITY);
        return new Response<>(filterApi.findAllFacilitiesByCityStateCountry(filter));
    }
}