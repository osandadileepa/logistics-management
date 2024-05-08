package com.quincus.shipment.impl.repository.entity.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FlightDetailsEntity {
    private String airportCode;
    private String airportName;
    private String scheduledTime;
    private String estimatedTime;
    private String actualTime;
    private String timezone;
}
