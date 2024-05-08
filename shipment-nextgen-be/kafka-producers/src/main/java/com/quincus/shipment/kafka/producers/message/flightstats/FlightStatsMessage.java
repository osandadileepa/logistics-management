package com.quincus.shipment.kafka.producers.message.flightstats;

import lombok.Data;

@Data
public class FlightStatsMessage {
    String eventId;
    String eventDateUtc;
    String correlationId;
    String module;
    String eventType;
    FlightEventPayloadMessage eventPayload;
}
