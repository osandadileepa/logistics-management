package com.quincus.qportal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QPortalParam {
    private String orderId;
    private String milestoneDatetime;
    private String trackingUrl;
    private String shipmentId;
    private String flightNumber;
    private String airline;
    private String airlineCode;
    private String facilityName;
    private String departureDateTime;
    private String arrivalDateTime;
}
