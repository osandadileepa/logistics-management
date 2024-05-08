package com.quincus.apigateway.api.dto;

import lombok.Data;

@Data
public class APIGFlightSchedule {
    private String carrier;
    private String carrierName;
    private String flightNumber;
    private String origin;
    private String departureTime;
    private String destination;
    private String arrivalTime;
    private String equipment;
    private String departureTerminal;
    private String serviceType;
}
