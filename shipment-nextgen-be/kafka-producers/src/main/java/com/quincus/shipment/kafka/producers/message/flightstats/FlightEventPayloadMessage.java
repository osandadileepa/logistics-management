package com.quincus.shipment.kafka.producers.message.flightstats;

import lombok.Data;

@Data
public class FlightEventPayloadMessage {
    private String carrier;
    private String flightNumber;
    private String departureDate;
    private String origin;
    private String destination;
    private boolean success;
    private String error;
    private String eventName;
    private String eventType;
    private String eventDate;
    private String flightId;
    private FlightStatusMessage flightStatus;
}
