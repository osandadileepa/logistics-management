package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Flight {
    @UUID(required = false)
    private String id;
    @Digits(integer = 20, fraction = 0)
    private Long flightId;
    @Size(max = 3)
    private String carrier;
    @Size(max = 60)
    private String flightNumber;
    @Size(max = 60)
    private String departureDate;
    @Size(max = 3)
    private String origin;
    @Size(max = 3)
    private String destination;
    private boolean success;
    private String error;
    private String eventDate;
    private FlightEventName eventName;
    private FlightEventType eventType;
    private FlightStatus flightStatus;
    private List<FlightStatus> flightStatuses;
}
