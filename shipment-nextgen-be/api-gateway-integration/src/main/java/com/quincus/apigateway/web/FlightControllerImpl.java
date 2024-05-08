package com.quincus.apigateway.web;

import com.quincus.apigateway.FlightController;
import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flight")
@AllArgsConstructor
public class FlightControllerImpl implements FlightController {
    private final ApiGatewayApi gatewayApi;

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_VIEW')")
    public Response<List<FlightSchedule>> searchFlightSchedules(final Request<FlightScheduleSearchParameter> request) {
        final FlightScheduleSearchParameter param = request.getData();
        final List<FlightSchedule> flightScheduleList = gatewayApi.searchFlights(param);
        return new Response<>(flightScheduleList);
    }
}
