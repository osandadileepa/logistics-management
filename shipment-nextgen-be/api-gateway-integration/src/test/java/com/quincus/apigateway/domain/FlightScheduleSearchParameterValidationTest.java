package com.quincus.apigateway.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


class FlightScheduleSearchParameterValidationTest extends ValidationTest {

    @Test
    void flightScheduleSearchParameter_withMissingMandatoryFields_shouldHaveViolations() {
        FlightScheduleSearchParameter flightScheduleSearchParameter = new FlightScheduleSearchParameter();

        var violations = ValidationTest.validator.validate(flightScheduleSearchParameter);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void flightScheduleSearchParameter_withValidFields_shouldNotHaveViolations() {
        FlightScheduleSearchParameter flightScheduleSearchParameter = new FlightScheduleSearchParameter();
        flightScheduleSearchParameter.setOrigin("ABC");
        flightScheduleSearchParameter.setDepartureDate(LocalDate.of(2023, 1, 1));
        flightScheduleSearchParameter.setDestination("XYZ");
        flightScheduleSearchParameter.setCarrier("AA");

        var violations = ValidationTest.validator.validate(flightScheduleSearchParameter);
        assertThat(violations).isEmpty();
    }

    @Test
    void flightScheduleSearchParameter_withBlankFields_shouldHaveViolations() {
        FlightScheduleSearchParameter flightScheduleSearchParameter = new FlightScheduleSearchParameter();
        flightScheduleSearchParameter.setOrigin(" ");
        flightScheduleSearchParameter.setDepartureDate(LocalDate.of(2023, 1, 1));
        flightScheduleSearchParameter.setDestination(" ");
        flightScheduleSearchParameter.setCarrier(" ");

        var violations = ValidationTest.validator.validate(flightScheduleSearchParameter);
        assertThat(violations).hasSize(2);
    }

    @Test
    void flightScheduleSearchParameter_withEmptyFields_shouldHaveViolations() {
        FlightScheduleSearchParameter flightScheduleSearchParameter = new FlightScheduleSearchParameter();
        flightScheduleSearchParameter.setOrigin("");
        flightScheduleSearchParameter.setDepartureDate(LocalDate.of(2023, 1, 1));
        flightScheduleSearchParameter.setDestination("");
        flightScheduleSearchParameter.setCarrier("");

        var violations = ValidationTest.validator.validate(flightScheduleSearchParameter);
        assertThat(violations).hasSize(4);
    }
}
