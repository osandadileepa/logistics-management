package com.quincus.qportal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QPortalParam {
    private String orderId;
    private String milestoneDateTime;
    private String trackingUrl;
    private String shipmentId;
    private String packageId;
    private String airlineCode;
    private String flightNumber;
    private String airline;
    private String facilityName;
    private String departureDateTime;
    private String arrivalDateTime;
    private String driverName;
    private String startTime;
}
