package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QPortalApiImplTest {

    @InjectMocks
    QPortalApiImpl qPortalApi;
    @Mock
    QPortalRestClient qPortalRestClient;

    @Test
    void listPackageTypes_ShouldReturnResult() {
        when(qPortalRestClient.listPackageTypes(anyString())).thenReturn(List.of(new QPortalPackageType()));
        assertThat(qPortalApi.listPackageTypes(anyString())).isNotNull();
    }

    @Test
    void listCostTypes_ShouldReturnResult() {
        when(qPortalRestClient.listCostTypes(anyString())).thenReturn(List.of(new QPortalCostType()));
        assertThat(qPortalApi.listCostTypes(anyString())).isNotNull();
    }

    @Test
    void listCurrencies_ShouldReturnResult() {
        when(qPortalRestClient.listCurrencies(anyString())).thenReturn(List.of(new QPortalCurrency()));
        assertThat(qPortalApi.listCurrencies(anyString())).isNotNull();
    }

    @Test
    void listDriversShouldReturnResult() {
        when(qPortalRestClient.listDrivers(anyString())).thenReturn(List.of(new QPortalDriver()));
        assertThat(qPortalApi.listDrivers(anyString())).isNotNull();
    }

    @Test
    void getCostType_ShouldReturnResult() {
        when(qPortalRestClient.getCostType(anyString(), anyString())).thenReturn(new QPortalCostType());
        assertThat(qPortalApi.getCostType(anyString(), anyString())).isNotNull();
    }

    @Test
    void getCurrency_ShouldReturnResult() {
        when(qPortalRestClient.getCurrency(anyString(), anyString())).thenReturn(new QPortalCurrency());
        assertThat(qPortalApi.getCurrency(anyString(), anyString())).isNotNull();
    }

    @Test
    void getUserById_ShouldReturnResult() {
        when(qPortalRestClient.getUserById(anyString(), anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getUser(anyString(), anyString())).isNotNull();
    }
}
