package com.quincus.apigateway;

import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/flight")
@Tag(name = "flights", description = "This endpoint manages flight transactions.")
@Validated
public interface FlightController {

    @PostMapping("/schedules")
    @Operation(summary = "Search Flight Schedules API", description = "Return a list of flight schedules based on request.", tags = "flights")
    Response<List<FlightSchedule>> searchFlightSchedules(@Valid @RequestBody final Request<FlightScheduleSearchParameter> request);
}
