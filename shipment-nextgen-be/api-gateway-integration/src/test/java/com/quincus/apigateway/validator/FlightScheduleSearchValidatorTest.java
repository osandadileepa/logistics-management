package com.quincus.apigateway.validator;

import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ValidationException;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class FlightScheduleSearchValidatorTest {

    @InjectMocks
    private FlightScheduleSearchValidator validator;

    @Test
    void validateFlightScheduleSearchParameterWhenValidParamThenShouldProceed() {
        var parameter = new FlightScheduleSearchParameter();
        parameter.setOrigin("XXX");
        parameter.setDestination("YYY");
        parameter.setDepartureDate(LocalDate.of(2023, 1, 1));
        assertThatNoException().isThrownBy(() -> validator.validateFlightScheduleSearchParameter(parameter));

    }

    @Test
    void validateFlightScheduleSearchParameterWhenIdenticalOriginAndDestinationThenShouldNotProceed() {
        var parameter = new FlightScheduleSearchParameter();
        parameter.setOrigin("XXX");
        parameter.setDestination("XXX");
        parameter.setDepartureDate(LocalDate.of(2023, 1, 1));
        assertThatThrownBy(() -> validator.validateFlightScheduleSearchParameter(parameter))
                .isInstanceOf(ValidationException.class);
    }
}
