package com.quincus.apigateway.validator;

import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;

@Component
public class FlightScheduleSearchValidator {

    private static final String ERR_SAME_AIRPORT = "Error: airports are identical. origin: '%s', destination: '%s'.";

    public void validateFlightScheduleSearchParameter(FlightScheduleSearchParameter searchParam) {
        if (searchParam.getOrigin().equalsIgnoreCase(searchParam.getDestination())) {
            throw new ValidationException(String.format(ERR_SAME_AIRPORT, searchParam.getOrigin(),
                    searchParam.getDestination()));
        }
    }
}
