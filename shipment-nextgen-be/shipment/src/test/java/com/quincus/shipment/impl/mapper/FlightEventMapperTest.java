package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FlightEventMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    FlightEventMapper flightEventMapper = new FlightEventMapperImpl();
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void convertMessageToFlightEventPayloadResponse_validParameters_shouldConvertToFlightEventPayloadResponse() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/flight-subscribe-rs.json");
        FlightStatsMessage flightStatsMessage = objectMapper.readValue(data.toString(), FlightStatsMessage.class);
        Flight flight = flightEventMapper.mapFlightEventPayloadMessageToFlight(flightStatsMessage.getEventPayload());
        assertThat(flight).isNotNull();
    }

    @Test
    void convertMessageToFlightEventDepartureDelay_validParameters_shouldConvertToFlightEventPayloadResponse() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/flight-time-departure-delay-rs.json");
        FlightStatsMessage flightStatsMessage = objectMapper.readValue(data.toString(), FlightStatsMessage.class);
        Flight flight = flightEventMapper.mapFlightEventPayloadMessageToFlight(flightStatsMessage.getEventPayload());
        assertThat(flight).isNotNull();
        assertThat(flight.getEventName()).isEqualTo(FlightEventName.DEPARTURE_DELAY);
    }

    @Test
    void convertMessageToFlightEventArrivalDelay_validParameters_shouldConvertToFlightEventPayloadResponse() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/flight-time-arrival-delay-rs.json");
        FlightStatsMessage flightStatsMessage = objectMapper.readValue(data.toString(), FlightStatsMessage.class);
        Flight flight = flightEventMapper.mapFlightEventPayloadMessageToFlight(flightStatsMessage.getEventPayload());
        assertThat(flight).isNotNull();
        assertThat(flight.getEventName()).isEqualTo(FlightEventName.ARRIVAL_DELAY);
    }

    @Test
    void convertMessageToFlightEventResponse_validParametersWithFlightStatus_shouldConvertToFlightEventResponse() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/flight-status-rs.json");
        FlightStatsMessage flightStatsMessage = objectMapper.readValue(data.toString(), FlightStatsMessage.class);
        Flight flight = flightEventMapper.mapFlightEventPayloadMessageToFlight(flightStatsMessage.getEventPayload());
        assertThat(flight.getEventName()).isEqualTo(FlightEventName.FLIGHT_LANDED);
        assertThat(flight.getFlightStatus().getDeparture())
                .extracting(FlightDetails::getScheduledTime, FlightDetails::getActualTime, FlightDetails::getEstimatedTime)
                .containsExactly("2022-11-29T12:57:00+08:00", "2022-11-29T12:59:00+08:00", "2022-11-29T12:58:00+08:00");
        assertThat(flight.getFlightStatus().getArrival())
                .extracting(FlightDetails::getScheduledTime, FlightDetails::getActualTime, FlightDetails::getEstimatedTime)
                .containsExactly("2022-11-29T13:03:00-08:00", "2022-11-29T12:54:00-08:00", "2022-11-29T13:04:00-08:00");
    }
}