package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.CostApi;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.api.filter.CostShipmentFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.service.CostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostApiImplTest {
    @Mock
    private CostService costService;

    private CostApi costApi;

    @BeforeEach
    public void setUp() {
        costApi = new CostApiImpl(costService);
    }

    @Test
    void shouldFind() {
        String id = "123";
        Cost expectedCost = new Cost();
        when(costService.find(id)).thenReturn(expectedCost);

        Cost actualCost = costApi.find(id);

        assertThat(actualCost).isEqualTo(expectedCost);
        verify(costService).find(id);
        verifyNoMoreInteractions(costService);
    }

    @Test
    void shouldCreate() {
        Cost cost = new Cost();
        Cost expectedCost = new Cost();

        when(costService.create(cost)).thenReturn(expectedCost);

        Cost actualCost = costApi.create(cost);

        assertThat(actualCost).isEqualTo(expectedCost);
        verify(costService).create(cost);
        verifyNoMoreInteractions(costService);
    }

    @Test
    void shouldUpdate() {
        String costId = "123";
        Cost cost = new Cost();
        Cost expectedCost = new Cost();

        when(costService.update(cost, costId)).thenReturn(expectedCost);

        Cost actualCost = costApi.update(cost, costId);

        assertThat(actualCost).isEqualTo(expectedCost);
        verify(costService).update(cost, costId);
        verifyNoMoreInteractions(costService);
    }

    @Test
    void shouldFindAllByCriteria() {
        CostFilter costFilter = new CostFilter();
        CostFilterResult expectedFilterResult = new CostFilterResult(List.of());

        when(costService.findAllByFilter(costFilter)).thenReturn(expectedFilterResult);

        CostFilterResult actualFilterResult = costApi.findAllByFilter(costFilter);
        assertThat(actualFilterResult).isEqualTo(expectedFilterResult);
        verify(costService).findAllByFilter(costFilter);
        verifyNoMoreInteractions(costService);
    }

    @Test
    void shouldFindByShipmentFilter() {
        ShipmentFilter filter = new ShipmentFilter();
        CostShipmentFilterResult expectedFilterResult = new CostShipmentFilterResult(List.of()).filter(filter);
        when(costService.findAllCostShipmentByFilter(filter)).thenReturn(expectedFilterResult);

        CostShipmentFilterResult actualFilterResult = costApi.findAllCostShipmentByFilter(filter);

        assertThat(actualFilterResult).isEqualTo(expectedFilterResult);
        verify(costService).findAllCostShipmentByFilter(filter);
        verifyNoMoreInteractions(costService);
    }

}
