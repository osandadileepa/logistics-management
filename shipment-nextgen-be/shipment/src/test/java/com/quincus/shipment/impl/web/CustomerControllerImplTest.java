package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.filter.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerControllerImplTest {
    private final int page = 1;
    private final String organizationId = "ORG1";
    private final String key = "KEY";
    private final int perPage = 10;
    @InjectMocks
    private CustomerControllerImpl locationController;
    @Mock
    private FilterApi filterApi;
    @Captor
    private ArgumentCaptor<Filter> captor;

    @Test
    void findServiceTypes_validParamsWithNoKey_shouldCallGenericFilterAPIAndHaveValidArgs() {
        locationController.findCustomers(perPage, page, null);
        verify(filterApi, times(1)).findCustomers(any());
        verify(filterApi).findCustomers(captor.capture());

        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
        assertThat(captor.getValue().getKey()).isNull();
    }

    @Test
    void findServiceTypes_validParamsWithKey_shouldCallGenericFilterAPIAndHaveValidArgs() {
        locationController.findCustomers(perPage, page, key);
        verify(filterApi, times(1)).findCustomers(any());
        verify(filterApi).findCustomers(captor.capture());

        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
        assertThat(captor.getValue().getKey()).isEqualTo(key.toLowerCase());
    }

    @Test
    void findCustomers_validParamsWithNoKey_shouldCallGenericFilterAPIAndHaveValidArgs() {
        locationController.findCustomers(perPage, page, null);
        verify(filterApi, times(1)).findCustomers(any());
        verify(filterApi).findCustomers(captor.capture());

        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
        assertThat(captor.getValue().getKey()).isNull();
    }

    @Test
    void findCustomers_validParamsWithKey_shouldCallGenericFilterAPIAndHaveValidArgs() {
        locationController.findCustomers(perPage, page, key);
        verify(filterApi, times(1)).findCustomers(any());
        verify(filterApi).findCustomers(captor.capture());

        assertThat(captor.getValue().getPerPage()).isEqualTo(perPage);
        assertThat(captor.getValue().getPage()).isEqualTo(page);
        assertThat(captor.getValue().getKey()).isEqualTo(key.toLowerCase());
    }
}
