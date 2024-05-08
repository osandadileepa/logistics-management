package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.orchestrator.FilterOrchestrator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FilterApiImpl implements FilterApi {

    private final FilterOrchestrator filterOrchestrator;

    @Override
    public FilterResult findLocationsByType(LocationFilter filter) {
        return filterOrchestrator.findLocationsByType(filter);
    }

    @Override
    public FilterResult findLocationHierarchies(LocationHierarchyFilter filter) {
        return filterOrchestrator.findLocationHierarchies(filter);
    }

    @Override
    public FilterResult findServiceTypes(Filter filter) {
        return filterOrchestrator.findServiceTypes(filter);
    }

    @Override
    public FilterResult findCustomers(Filter filter) {
        return filterOrchestrator.findCustomers(filter);
    }

    @Override
    public FilterResult findAllStatesByCountry(LocationHierarchyFilter filter) {
        return filterOrchestrator.findAllStatesByCountry(filter);
    }

    @Override
    public FilterResult findAllCitiesByStateCountry(LocationHierarchyFilter filter) {
        return filterOrchestrator.findAllCitiesByState(filter);
    }

    @Override
    public FilterResult findAllFacilitiesByCityStateCountry(LocationHierarchyFilter filter) {
        return filterOrchestrator.findAllFacilitiesByCity(filter);
    }

    @Override
    public FilterResult findServiceTypesForNetworkLane(Filter filter) {
        return filterOrchestrator.findServiceTypesForNetworkLane(filter);
    }
}
