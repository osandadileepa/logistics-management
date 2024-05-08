package com.quincus.apigateway.web;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightControllerImplTest {

    @InjectMocks
    private FlightControllerImpl flightControllerImpl;
    @Mock
    private ApiGatewayApi apiGatewayApi;

    @Test
    void searchFlightSchedulesWhenValidDataThenShouldReturnSuccess() {
        FlightSchedule flightScheduleDummy = new FlightSchedule();
        when(apiGatewayApi.searchFlights(any(FlightScheduleSearchParameter.class)))
                .thenReturn(List.of(flightScheduleDummy));
        FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);
        final Response<List<FlightSchedule>> response = flightControllerImpl.searchFlightSchedules(request);
        final List<FlightSchedule> flightScheduleList = response.getData();
        assertThat(flightScheduleList).isNotEmpty();
    }
}
