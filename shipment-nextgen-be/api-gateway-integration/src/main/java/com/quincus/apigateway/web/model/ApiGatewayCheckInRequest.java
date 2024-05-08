package com.quincus.apigateway.web.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApiGatewayCheckInRequest {
    private String orderNo;
    private BigDecimal segmentId;
    private String jobType;
    private String awbNumber;
    private String locationType;
    private String driverID;
    private String driverPhoneNumber;
    private String driverName;
    private String checkInTime;
    private BigDecimal checkInLatitude;
    private BigDecimal checkInLongitude;
    private String timestamp;
}
