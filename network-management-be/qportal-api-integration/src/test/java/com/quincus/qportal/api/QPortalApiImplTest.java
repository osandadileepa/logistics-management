package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCurrency;
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
    void listCurrencies_ShouldReturnResult() {
        when(qPortalRestClient.listCurrencies(anyString())).thenReturn(List.of(new QPortalCurrency()));
        assertThat(qPortalApi.listCurrencies(anyString())).isNotNull();
    }

    @Test
    void getCurrency_ShouldReturnResult() {
        when(qPortalRestClient.getCurrencyById(anyString(), anyString())).thenReturn(new QPortalCurrency());
        assertThat(qPortalApi.getCurrencyById(anyString(), anyString())).isNotNull();
    }

    @Test
    void getUserById_ShouldReturnResult() {
        when(qPortalRestClient.getUserById(anyString(), anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getUserById(anyString(), anyString())).isNotNull();
    }
}
