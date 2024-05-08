package com.quincus.apigateway.api.dto;

import lombok.Data;

@Data
public class APIGFlightScheduleSearchParameter {
    private String origin;
    private String departureDate;
    private String destination;
    private String carrier;
}
