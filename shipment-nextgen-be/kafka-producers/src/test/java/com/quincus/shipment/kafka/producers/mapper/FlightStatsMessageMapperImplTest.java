package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.constant.FlightStatsEventType;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class FlightStatsMessageMapperImplTest {
    @InjectMocks
    FlightStatsMessageMapperImpl flightStatsMapper;

    @Test
    void mapToFlightStatsMessage_withValidParameters_shouldReturnFlightStatsMessage() {
        String uuid = randomUUID().toString();
        FlightEventPayloadMessage message = createFlightEventPayloadMessage();

        FlightStatsMessage result = flightStatsMapper.mapToFlightStatsMessage(message, uuid);
        assertThat(result.getEventId()).isEqualTo(uuid);
        assertThat(result.getEventType()).isEqualTo(FlightStatsEventType.FLIGHT_SUBSCRIBE_RQ.toString());
        assertThat(result.getEventPayload()).isNotNull();
    }

    @Test
    void mapFlightToEventPayloadMessage_withValidParameters_shouldReturnFlightEventPayloadMessage() {
        FlightStatsRequest request = createFlightEventPayloadRequest();

        FlightEventPayloadMessage result = flightStatsMapper.mapFlightToEventPayloadMessage(request);
        assertThat(result.getCarrier()).isEqualTo(request.getCarrier());
        assertThat(result.getFlightNumber()).isEqualTo(request.getFlightNumber());
        assertThat(result.getOrigin()).isEqualTo(request.getOrigin());
        assertThat(result.getDestination()).isEqualTo(request.getDestination());
        assertThat(LocalDate.parse(result.getDepartureDate())).isEqualTo(request.getDepartureDate());
    }

    private FlightEventPayloadMessage createFlightEventPayloadMessage() {
        FlightEventPayloadMessage message = new FlightEventPayloadMessage();
        message.setCarrier("QF");
        message.setOrigin("SYD");
        message.setDestination("MNL");
        message.setFlightNumber("19");
        message.setDepartureDate("2023-02-18");
        return message;
    }


    private FlightEventPayloadMessage createEventPayloadWithFlightStatusMessage() {
        FlightEventPayloadMessage message = new FlightEventPayloadMessage();
        message.setCarrier("QF");
        message.setOrigin("SYD");
        message.setDestination("MNL");
        message.setFlightNumber("19");
        message.setDepartureDate("2023-02-18");
        return message;
    }

    private FlightStatsRequest createFlightEventPayloadRequest() {
        FlightStatsRequest request = new FlightStatsRequest();
        request.setCarrier("QF");
        request.setOrigin("SYD");
        request.setDestination("MNL");
        request.setFlightNumber("19");
        request.setDepartureDate(LocalDate.parse("2023-02-18"));
        return request;
    }
}
