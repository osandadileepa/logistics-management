package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.LocationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocationControllerImplTest {
    @InjectMocks
    private LocationControllerImpl locationController;
    @Mock
    private FilterApi filterApi;

    @Test
    void all_validParams_shouldReturnFilterResult() {
        String organizationId = "ORG1";
        LocationType type = LocationType.CITY;

        locationController.findLocations(type, 0, 0, "", "name");

        verify(filterApi, times(1)).findLocationsByType(any(LocationFilter.class));
    }

}
