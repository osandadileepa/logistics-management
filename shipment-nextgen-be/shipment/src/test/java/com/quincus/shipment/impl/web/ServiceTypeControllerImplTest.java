package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.filter.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceTypeControllerImplTest {
    @InjectMocks
    private ServiceTypeControllerImpl serviceTypeController;
    @Mock
    private FilterApi filterApi;

    @Test
    void allServiceTypes_validParams_shouldReturnFilterResult() {
        serviceTypeController.findServiceTypes(0, 0, "");
        verify(filterApi, times(1)).findServiceTypes(any(Filter.class));
    }

    @Test
    void allServiceTypesForNetworkLane_validParams_shouldReturnFilterResult() {
        serviceTypeController.findServiceTypesForNetworkLane(0, 0, "");
        verify(filterApi, times(1)).findServiceTypesForNetworkLane(any(Filter.class));
    }
}
