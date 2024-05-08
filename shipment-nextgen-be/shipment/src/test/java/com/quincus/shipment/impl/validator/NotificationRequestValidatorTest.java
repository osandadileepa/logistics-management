package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRequestValidatorTest {

    private final NotificationRequestValidator notificationRequestValidator = new NotificationRequestValidator();

    @Test
    void shouldThrowExceptionWhenShipmentTrackingIdIsNull() {
        Shipment shipment = new Shipment();
        shipment.setShipmentTrackingId(null);
        Milestone milestone = new Milestone();
        milestone.setPartnerId(UUID.randomUUID().toString());
        milestone.setMilestoneTime(OffsetDateTime.now());

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);

        assertThatThrownBy(() -> notificationRequestValidator.validateShipmentReturnedParams(notificationRequest))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Missing parameters: Shipment tracking ID");
    }

    @Test
    void shouldThrowExceptionWhenMilestoneDateTimeIsNull() {
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(null);
        milestone.setPartnerId(UUID.randomUUID().toString());

        Shipment shipment = new Shipment();
        shipment.setShipmentTrackingId(null);

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);

        assertThatThrownBy(() -> notificationRequestValidator.validateShipmentReturnedParams(notificationRequest))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Missing parameters: Shipment tracking ID, Milestone datetime");
    }

    @Test
    void shouldThrowExceptionWhenFlightNumberIsNull() {
        Flight flight = new Flight();
        flight.setFlightNumber(null);
        flight.setFlightStatus(new FlightStatus());

        // Set airline name
        flight.getFlightStatus().setAirlineName("Airline");

        // Set arrival and departure details
        FlightDetails departureDetails = new FlightDetails();
        departureDetails.setAirportName("Departure Airport");
        departureDetails.setActualTime("12:00");
        flight.getFlightStatus().setDeparture(departureDetails);

        // Set arrival details but with missing parameters
        FlightDetails arrivalDetails = new FlightDetails();
        arrivalDetails.setAirportName(null);
        arrivalDetails.setActualTime(null);
        flight.getFlightStatus().setArrival(arrivalDetails);

        Shipment shipment = new Shipment();
        shipment.setPartnerId(UUID.randomUUID().toString());

        Milestone milestone = new Milestone();
        milestone.setPartnerId(UUID.randomUUID().toString());

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setFlight(flight);
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);


        assertThatThrownBy(() -> notificationRequestValidator.validateFlightDepartedParams(notificationRequest))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Missing parameters: Flight number");
    }

    @Test
    void shouldNotThrowExceptionWhenAllRequiredParametersArePresent() {
        Order order = new Order();
        order.setOrderIdLabel("label");
        order.setTrackingUrl("url");

        Shipment shipment = new Shipment();
        shipment.setShipmentTrackingId("123");
        shipment.setOrder(order);

        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setPartnerId(UUID.randomUUID().toString());

        Flight flight = new Flight();
        flight.setFlightNumber("FN123");
        flight.setFlightStatus(new FlightStatus());
        flight.getFlightStatus().setAirlineName("Airline");
        flight.getFlightStatus().setDeparture(new FlightDetails());
        flight.getFlightStatus().getDeparture().setAirportName("Departure Airport");
        flight.getFlightStatus().getDeparture().setActualTime("Departure Time");

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);
        notificationRequest.setFlight(flight);

        // No exception should be thrown
        notificationRequestValidator.validateDeliverySuccessfulParams(notificationRequest);
    }

    @Test
    void shouldThrowExceptionWhenTrackingUrlIsNull() {
        Order order = new Order();
        order.setTrackingUrl(null);
        order.setOrderIdLabel("idLabel");

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setShipmentTrackingId("123");

        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setPartnerId(UUID.randomUUID().toString());

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);

        assertThatThrownBy(() ->
                notificationRequestValidator.validateDeliverySuccessfulParams(notificationRequest))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Missing parameters: Tracking URL");
    }

    @Test
    void shouldThrowExceptionWhenFlightDetailsAreMissingInFlightArrival() {
        Flight flight = new Flight();
        flight.setFlightNumber("FN123");
        flight.setFlightStatus(new FlightStatus());

        // Set airline name
        flight.getFlightStatus().setAirlineName("Airline");

        // Set arrival and departure details
        FlightDetails departureDetails = new FlightDetails();
        departureDetails.setAirportName("Departure Airport");
        departureDetails.setActualTime("12:00");
        flight.getFlightStatus().setDeparture(departureDetails);

        // Set arrival details but with missing parameters
        FlightDetails arrivalDetails = new FlightDetails();
        arrivalDetails.setAirportName(null);
        arrivalDetails.setActualTime(null);
        flight.getFlightStatus().setArrival(arrivalDetails);

        Shipment shipment = new Shipment();
        shipment.setPartnerId(UUID.randomUUID().toString());

        Milestone milestone = new Milestone();
        milestone.setPartnerId(UUID.randomUUID().toString());

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setFlight(flight);
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);

        assertThatThrownBy(() ->
                notificationRequestValidator.validateFlightArrivedParams(notificationRequest))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Missing parameters: Arrival airport name, Arrival time");
    }

}