package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.constant.FlightStatusResult;
import com.quincus.shipment.impl.service.FlightStatsEventPostProcessService;
import com.quincus.shipment.impl.service.FlightStatsEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightStatsApiImplTest {
    @InjectMocks
    private FlightStatsApiImpl flightStatsApi;

    @Mock
    private FlightStatsEventPostProcessService flightStatsEventPostProcessService;

    @Mock
    private FlightStatsEventService flightStatsEventService;

    @Test
    void givenMessageAndTrxId_whenReceiveFlightStatsMessage_verifyAllServiceClls() {
        String message = "test message";
        String trxId = "test trxId";

        FlightStatusResult result = mock(FlightStatusResult.class);
        when(flightStatsEventService.processFlightStatsMessage(message, trxId)).thenReturn(result);

        //WHEN:
        flightStatsApi.receiveFlightStatsMessage(message, trxId);

        //THEN:
        verify(flightStatsEventService, times(1)).processFlightStatsMessage(message, trxId);
        verify(flightStatsEventPostProcessService, times(1)).processAlerts(result);
    }
}