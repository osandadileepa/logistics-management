package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import com.quincus.shipment.impl.orchestrator.FilterOrchestrator;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.valueobject.LocationHierarchyTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @InjectMocks
    private FilterOrchestrator filterOrchestrator;

    @Mock
    private ServiceTypeService serviceTypeService;

    @Mock
    private CustomerService customerService;

    @Mock
    private LocationHierarchyService locationHierarchyService;

    @Mock
    private LocationService locationService;

    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;

    @Test
    void findLocationsByType_locationFound_shouldReturnGenericFilterResult() {
        LocationFilter locationFilter = generateLocationFilter();

        when(locationService.findPageableLocationsByFilter(locationFilter))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        FilterResult filterResult = filterOrchestrator.findLocationsByType(locationFilter);

        assertThat(filterResult.getFilter()).isNotNull().isEqualTo(locationFilter);
        verify(locationService, times(1)).findPageableLocationsByFilter(locationFilter);
    }

    @Test
    void findLocationsByType_locationNotFound_shouldReturnEmptyGenericFilterResult() {
        LocationFilter locationFilter = generateLocationFilter();

        when(locationService.findPageableLocationsByFilter(locationFilter))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        FilterResult filterResult = filterOrchestrator.findLocationsByType(locationFilter);

        assertThat(filterResult.getFilter()).isNotNull().isEqualTo(locationFilter);
        assertThat(filterResult.getResult()).isEmpty();
        verify(locationService, times(1)).findPageableLocationsByFilter(any());
    }

    @Test
    void findLocationsByType_nullArguments_shouldReturnEmptyGenericFilterResult() {
        FilterResult filterResult = filterOrchestrator.findLocationsByType(null);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getFilter()).isNull();
        assertThat(filterResult.getResult()).isEmpty();
        verify(locationService, never()).findPageableLocationsByFilter(any());
    }

    @Test
    void findLocationHierarchyWithoutKey_locationFound_shouldReturnGenericFilterResult() {
        LocationHierarchyFilter genericFilter = generateLocationHierarchyFilter();
        List<LocationHierarchyTree> locationHierarchyTrees = new ArrayList<>();
        LocationHierarchyTree locationHierarchyTree = new LocationHierarchyTree(new LocationEntity(), "test");
        locationHierarchyTrees.add(locationHierarchyTree);

        when(locationHierarchyService.findPageableLocationHierarchiesByFilter(genericFilter))
                .thenReturn(new PageImpl<>(locationHierarchyTrees));

        FilterResult filterResult = filterOrchestrator.findLocationHierarchies(genericFilter);

        assertThat(filterResult).isNotNull();
        //TODO assertThat(filterResult.getResult().get(0)).isInstanceOf(Object.class);
        assertThat(filterResult.getFilter()).isEqualTo(genericFilter);
        verify(locationHierarchyService, times(1)).findPageableLocationHierarchiesByFilter(genericFilter);
    }

    @Test
    void findLocationHierarchyWithoutKey_locationNotFound_shouldReturnEmptyGenericFilterResult() {
        LocationHierarchyFilter locationHierarchyFilter = generateLocationHierarchyFilter();
        locationHierarchyFilter.setKey(null);
        List<LocationHierarchyTree> locationHierarchyTrees = new ArrayList<>();

        when(locationHierarchyService.findPageableLocationHierarchiesByFilter(any()))
                .thenReturn(new PageImpl<>(locationHierarchyTrees));

        FilterResult filterResult = filterOrchestrator.findLocationHierarchies(locationHierarchyFilter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isEqualTo(locationHierarchyFilter);
        verify(locationHierarchyService, times(1)).findPageableLocationHierarchiesByFilter(any());
    }

    @Test
    void findLocationHierarchyWithKey_locationFound_shouldReturnGenericFilterResult() {
        LocationHierarchyFilter locationHierarchyFilter = generateLocationHierarchyFilter();

        List<LocationHierarchyTree> locationHierarchyTrees = new ArrayList<>();
        LocationHierarchyTree locationHierarchyTree = new LocationHierarchyTree(new LocationEntity(), "test");
        locationHierarchyTrees.add(locationHierarchyTree);

        when(locationHierarchyService.findPageableLocationHierarchiesByFilter(locationHierarchyFilter))
                .thenReturn(new PageImpl<>(locationHierarchyTrees));

        FilterResult filterResult = filterOrchestrator.findLocationHierarchies(locationHierarchyFilter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getFilter()).isEqualTo(locationHierarchyFilter);
        verify(locationHierarchyService, times(1)).findPageableLocationHierarchiesByFilter(any());
    }

    @Test
    void findLocationHierarchyWithKey_locationNotFound_shouldReturnEmptyGenericFilterResult() {
        LocationHierarchyFilter locationHierarchyFilter = generateLocationHierarchyFilter();
        List<LocationHierarchyTree> locationHierarchyTrees = new ArrayList<>();
        when(locationHierarchyService.findPageableLocationHierarchiesByFilter(locationHierarchyFilter))
                .thenReturn(new PageImpl<>(locationHierarchyTrees));

        FilterResult filterResult = filterOrchestrator.findLocationHierarchies(locationHierarchyFilter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isEqualTo(locationHierarchyFilter);
    }

    @Test
    void findLocationHierarchy_nullArguments_shouldReturnEmptyGenericFilterResult() {
        FilterResult filterResult = filterOrchestrator.findLocationHierarchies(null);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isNull();
        verify(locationHierarchyService, never()).findPageableLocationHierarchiesByFilter(any());
    }

    @Test
    void findAllServiceTypes_serviceTypeFound_shouldReturnGenericFilterResult() {
        Filter filter = generateGenericFilter();

        List<ServiceTypeEntity> serviceTypeEntities = new ArrayList<>();
        ServiceTypeEntity dummyServiceTypeEntity = new ServiceTypeEntity();
        dummyServiceTypeEntity.setOrganizationId(new OrganizationEntity().getId());
        dummyServiceTypeEntity.setName("Service Type 1");
        serviceTypeEntities.add(dummyServiceTypeEntity);

        when(serviceTypeService.findPageableServiceTypesByFilter(filter))
                .thenReturn(new PageImpl<>(serviceTypeEntities));

        FilterResult filterResult = filterOrchestrator.findServiceTypes(filter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult().get(0)).isEqualTo(dummyServiceTypeEntity);
        assertThat(filterResult.getFilter()).isEqualTo(filter);
        verify(serviceTypeService, times(1)).findPageableServiceTypesByFilter(filter);
    }

    @Test
    void findAllServiceTypes_serviceTypeNotFound_shouldReturnEmptyGenericFilterResult() {
        Filter filter = generateGenericFilter();

        when(serviceTypeService.findPageableServiceTypesByFilter(any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        FilterResult filterResult = filterOrchestrator.findServiceTypes(filter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isEqualTo(filter);
        verify(serviceTypeService, times(1)).findPageableServiceTypesByFilter(filter);
    }

    @Test
    void findAllServiceTypes_nullArguments_shouldReturnEmptyGenericFilterResult() {
        FilterResult filterResult = filterOrchestrator.findServiceTypes(null);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isNull();
        verify(serviceTypeService, never()).findPageableServiceTypesByFilter(any());
    }

    @Test
    void findCustomers_customerFound_shouldReturnGenericFilterResult() {
        Filter filter = generateGenericFilter();

        List<CustomerEntity> customerEntities = new ArrayList<>();
        CustomerEntity dummyCustomerEntity = new CustomerEntity();
        dummyCustomerEntity.setName("CUSTOMER 1");
        dummyCustomerEntity.setOrganizationId("Dummy");
        customerEntities.add(dummyCustomerEntity);

        when(customerService.findPageableCustomersByFilter(filter)).thenReturn(new PageImpl<>(customerEntities));

        FilterResult filterResult = filterOrchestrator.findCustomers(filter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult().get(0)).isEqualTo(dummyCustomerEntity);
        assertThat(filterResult.getFilter()).isEqualTo(filter);
        verify(customerService, times(1)).findPageableCustomersByFilter(filter);
    }

    @Test
    void findCustomers_customerNotFound_shouldReturnEmptyGenericFilterResult() {
        Filter filter = generateGenericFilter();

        when(customerService.findPageableCustomersByFilter(filter)).thenReturn(new PageImpl<>(Collections.emptyList()));

        FilterResult filterResult = filterOrchestrator.findCustomers(filter);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isEqualTo(filter);
        verify(customerService, times(1)).findPageableCustomersByFilter(filter);
    }

    @Test
    void findCustomers_nullArguments_shouldReturnEmptyGenericFilterResult() {
        FilterResult filterResult = filterOrchestrator.findCustomers(null);

        assertThat(filterResult).isNotNull();
        assertThat(filterResult.getResult()).isEmpty();
        assertThat(filterResult.getFilter()).isNull();
        verify(customerService, never()).findByOrganizationId();
    }

    private LocationHierarchyFilter generateLocationHierarchyFilter() {
        LocationHierarchyFilter locationHierarchyFilter = new LocationHierarchyFilter();
        locationHierarchyFilter.setPage(1);
        locationHierarchyFilter.setPerPage(30);
        locationHierarchyFilter.setKey("o");

        return locationHierarchyFilter;
    }

    private LocationFilter generateLocationFilter() {

        LocationFilter locationFilter = new LocationFilter();
        locationFilter.setType(LocationType.COUNTRY);
        locationFilter.setPage(1);
        locationFilter.setPerPage(30);
        locationFilter.setKey("o");

        return locationFilter;
    }

    private Filter generateGenericFilter() {
        Filter filter = new Filter();
        filter.setPage(1);
        filter.setPerPage(10);
        filter.setKey("e");
        return filter;
    }
}