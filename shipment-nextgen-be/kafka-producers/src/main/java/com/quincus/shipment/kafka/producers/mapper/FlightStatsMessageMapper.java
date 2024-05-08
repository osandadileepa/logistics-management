package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;

public interface FlightStatsMessageMapper {
    FlightStatsMessage mapToFlightStatsMessage(FlightEventPayloadMessage eventPayload, String uuid);

    FlightEventPayloadMessage mapFlightToEventPayloadMessage(FlightStatsRequest flight);
}
