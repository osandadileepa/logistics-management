package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.orchestrator.FilterOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FilterApiImplTest {

    @InjectMocks
    private FilterApiImpl filterApi;

    @Mock
    private FilterOrchestrator filterOrchestrator;

    @Test
    void shouldCallFindLocationsByTypeOnce() {
        filterApi.findLocationsByType(new LocationFilter());
        verify(filterOrchestrator, times(1)).findLocationsByType(any(LocationFilter.class));
    }

    @Test
    void shouldCallFindLocationHierarchyOnce() {
        filterApi.findLocationHierarchies(new LocationHierarchyFilter());
        verify(filterOrchestrator, times(1)).findLocationHierarchies(any(LocationHierarchyFilter.class));
    }

    @Test
    void shouldCallFindAllServiceTypesOnce() {
        filterApi.findServiceTypes(new Filter());
        verify(filterOrchestrator, times(1)).findServiceTypes(any(Filter.class));
    }

    @Test
    void shouldCallFindCustomersOnce() {
        filterApi.findCustomers(new Filter());
        verify(filterOrchestrator, times(1)).findCustomers(any(Filter.class));
    }

    @Test
    void shouldCallFindAllServiceTypesForNetworkLaneOnce() {
        filterApi.findServiceTypesForNetworkLane(new Filter());
        verify(filterOrchestrator, times(1)).findServiceTypesForNetworkLane(any(Filter.class));
    }

}
