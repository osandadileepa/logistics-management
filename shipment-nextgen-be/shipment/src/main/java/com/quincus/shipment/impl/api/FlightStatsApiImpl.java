package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.FlightStatsApi;
import com.quincus.shipment.api.constant.FlightStatusResult;
import com.quincus.shipment.impl.service.FlightStatsEventPostProcessService;
import com.quincus.shipment.impl.service.FlightStatsEventService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FlightStatsApiImpl implements FlightStatsApi {
    private final FlightStatsEventPostProcessService flightStatsEventPostProcessService;
    private final FlightStatsEventService flightStatsEventService;

    @Override
    public void receiveFlightStatsMessage(String payload, String messageTransactionId) {
        FlightStatusResult flightStatusResult = flightStatsEventService.processFlightStatsMessage(payload, messageTransactionId);
        flightStatsEventPostProcessService.processAlerts(flightStatusResult);
    }
}
