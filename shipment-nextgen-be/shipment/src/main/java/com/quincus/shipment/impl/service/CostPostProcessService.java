package com.quincus.shipment.impl.service;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.domain.Cost;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CostPostProcessService {

    private static final String QLOGGER_UPDATE_EVENT_SOURCE = "CostService#update";
    private static final String QLOGGER_CREATE_EVENT_SOURCE = "CostService#create";

    private final QLoggerAPI qLoggerAPI;
    private final ApiGatewayApi apiGatewayApi;

    @Async("externalApiExecutor")
    public void publishCostCreatedEventAndSendAdditionalCharges(Cost cost) {
        qLoggerAPI.publishCostCreatedEvent(QLOGGER_CREATE_EVENT_SOURCE, cost);
        apiGatewayApi.sendUpdateOrderAdditionalCharges(cost);
    }

    @Async("externalApiExecutor")
    public void publishCostUpdatedEventAndSendAdditionalCharges(Cost cost) {
        qLoggerAPI.publishCostUpdatedEvent(QLOGGER_UPDATE_EVENT_SOURCE, cost);
        apiGatewayApi.sendUpdateOrderAdditionalCharges(cost);
    }

}
