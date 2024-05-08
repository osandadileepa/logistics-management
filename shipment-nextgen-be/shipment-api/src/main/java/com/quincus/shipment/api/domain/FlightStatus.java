package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.constant.FlightStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FlightStatus {
    private String id;
    private Long flightId;
    private FlightStatusCode status;
    private FlightDetails departure;
    private FlightDetails arrival;
    private String airlineCode;
    private String airlineName;
    private String operatingAirlineCode;
    private String longitude;
    private String latitude;
    private String speedMph;
    private String altitudeFt;
    private String error;
    private boolean success;
    private String eventDate;
    private FlightEventType eventType;
    private FlightEventName eventName;
}
