package com.quincus.shipment.impl.orchestrator;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.service.CustomerService;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.LocationService;
import com.quincus.shipment.impl.service.ServiceTypeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@AllArgsConstructor
public class FilterOrchestrator {

    private final CustomerService customerService;

    private final LocationHierarchyService locationHierarchyService;

    private final LocationService locationService;

    private final ServiceTypeService serviceTypeService;

    private FilterResult toFilterResult(Page<?> page, Filter filter) {
        List<?> contents = null;
        long totalElements = 0;
        int totalPages = 0;
        if (page != null) {
            contents = page.getContent();
            totalElements = page.getTotalElements();
            totalPages = page.getTotalPages();
        }
        FilterResult filterResult = new FilterResult(contents, filter);
        filterResult.setTotalElements(totalElements);
        filterResult.setTotalPages(totalPages);
        filterResult.setPage(filter.getPage());
        return filterResult;
    }

    public FilterResult findAllStatesByCountry(LocationHierarchyFilter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(locationHierarchyService.findAllStatesByCountry(filter), filter);
    }

    public FilterResult findAllCitiesByState(LocationHierarchyFilter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(locationHierarchyService.findAllCitiesByState(filter), filter);
    }

    public FilterResult findAllFacilitiesByCity(LocationHierarchyFilter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(locationHierarchyService.findAllFacilitiesByCity(filter), filter);
    }

    public FilterResult findLocationsByType(LocationFilter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(locationService.findPageableLocationsByFilter(filter), filter);
    }

    public FilterResult findLocationHierarchies(LocationHierarchyFilter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(locationHierarchyService.findPageableLocationHierarchiesByFilter(filter), filter);
    }

    public FilterResult findServiceTypes(Filter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(serviceTypeService.findPageableServiceTypesByFilter(filter), filter);
    }

    public FilterResult findCustomers(Filter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(customerService.findPageableCustomersByFilter(filter), filter);
    }

    public FilterResult findServiceTypesForNetworkLane(Filter filter) {
        if (filter == null) {
            return new FilterResult(Collections.emptyList(), null);
        }
        return toFilterResult(serviceTypeService.findPageableServiceTypesByFilterForNetworkLane(filter), filter);
    }
}