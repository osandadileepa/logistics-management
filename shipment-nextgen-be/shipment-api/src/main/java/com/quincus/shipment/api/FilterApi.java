package com.quincus.shipment.api;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;

public interface FilterApi {

    FilterResult findLocationsByType(LocationFilter filter);

    FilterResult findLocationHierarchies(LocationHierarchyFilter filter);

    FilterResult findServiceTypes(Filter filter);

    FilterResult findCustomers(Filter filter);

    FilterResult findAllStatesByCountry(LocationHierarchyFilter filter);

    FilterResult findAllCitiesByStateCountry(LocationHierarchyFilter filter);

    FilterResult findAllFacilitiesByCityStateCountry(LocationHierarchyFilter filter);

    FilterResult findServiceTypesForNetworkLane(Filter filter);
}
