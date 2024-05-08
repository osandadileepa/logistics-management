package com.quincus.shipment.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FlightDetails {
    private String airportCode;
    private String airportName;
    private String scheduledTime;
    private String estimatedTime;
    private String actualTime;
    private String timezone;
}
