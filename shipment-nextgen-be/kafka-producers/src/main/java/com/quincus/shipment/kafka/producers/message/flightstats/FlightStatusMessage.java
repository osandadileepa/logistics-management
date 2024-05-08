package com.quincus.shipment.kafka.producers.message.flightstats;

import lombok.Data;

@Data
public class FlightStatusMessage {
    private String status;
    private String flightId;
    private String departureAirportCode;
    private String departureAirportName;
    private String arrivalAirportCode;
    private String arrivalAirportName;
    private String scheduledDeparture;
    private String estimatedDeparture;
    private String actualDeparture;
    private String scheduledArrival;
    private String estimatedArrival;
    private String actualArrival;
    private String airlineCode;
    private String airlineName;
    private String operatingAirlineCode;
    private String longitude;
    private String latitude;
    private String speedMph;
    private String altitudeFt;
    private String eventType;
    private String eventDate;
    private String eventName;
}
