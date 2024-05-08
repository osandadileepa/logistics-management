package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.filter.LocationHierarchyFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocationHierarchyControllerImplTest {
    @InjectMocks
    private LocationHierarchyControllerImpl locationHierarchiesController;
    @Mock
    private FilterApi filterApi;

    @Test
    void shouldFindLocationHierarchies() {
        String organizationId = "ORG1";
        String countryId = "002-US";
        String stateId = "180-MA";
        String cityId = "1037-BOSTON";
        String facilityId = "FACI-ONE";

        locationHierarchiesController.findLocationHierarchies(countryId, stateId, cityId, facilityId, 0, 0, "", 3);

        verify(filterApi, times(1)).findLocationHierarchies(any(LocationHierarchyFilter.class));
    }

    @Test
    void shouldFindAllFacilitiesByCityStateCountry() {
        String organizationId = "ORG1";
        String countryId = "002-US";
        String stateId = "180-MA";
        String cityId = "1037-BOSTON";

        locationHierarchiesController.findAllFacilitiesByCityStateCountry(countryId, stateId, cityId, 0, 0, "");

        verify(filterApi, times(1)).findAllFacilitiesByCityStateCountry(any(LocationHierarchyFilter.class));
    }

    @Test
    void shouldFindAllCitiesByStateCountry() {
        String organizationId = "ORG1";
        String countryId = "002-US";
        String stateId = "180-MA";

        locationHierarchiesController.findAllCitiesByStateCountry(countryId, stateId, 0, 0, "");

        verify(filterApi, times(1)).findAllCitiesByStateCountry(any(LocationHierarchyFilter.class));
    }

    @Test
    void shouldFindAllStatesByCountry() {
        String organizationId = "ORG1";
        String countryId = "002-US";

        locationHierarchiesController.findAllStatesByCountry(countryId, 0, 0, "");

        verify(filterApi, times(1)).findAllStatesByCountry(any(LocationHierarchyFilter.class));
    }
}