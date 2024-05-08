package com.quincus.apigateway.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FlightSchedule {
    private String carrier;
    private String flightNumber;
    private String carrierName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXXXX")
    private OffsetDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXXXX")
    private OffsetDateTime arrivalTime;
}
