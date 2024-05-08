package com.quincus.shipment.impl.service;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.domain.Cost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CostPostProcessServiceTest {
    private static final String QLOGGER_CREATE_EVENT_SOURCE = "CostService#create";
    private static final String QLOGGER_UPDATE_EVENT_SOURCE = "CostService#update";
    private final Cost cost = new Cost();
    @InjectMocks
    private CostPostProcessService costPostProcessService;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private ApiGatewayApi apiGatewayApi;

    @Test
    void whenPublishCostCreatedEventAndSendAdditionalCharges_thenDependenciesAreCalled() {
        costPostProcessService.publishCostCreatedEventAndSendAdditionalCharges(cost);
        verify(qLoggerAPI).publishCostCreatedEvent(QLOGGER_CREATE_EVENT_SOURCE, cost);
        verify(apiGatewayApi).sendUpdateOrderAdditionalCharges(cost);
    }

    @Test
    void whenPublishCostUpdatedEventAndSendAdditionalCharges_thenDependenciesAreCalled() {
        costPostProcessService.publishCostUpdatedEventAndSendAdditionalCharges(cost);
        verify(qLoggerAPI).publishCostUpdatedEvent(QLOGGER_UPDATE_EVENT_SOURCE, cost);
        verify(apiGatewayApi).sendUpdateOrderAdditionalCharges(cost);
    }
}
