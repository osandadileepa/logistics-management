package com.quincus.apigateway.web.model;

import lombok.Data;

@Data
public class ApiGatewayAssignVendorDetailRequest {
    private String orderNo;
    private String segmentId;
    private String vendorId;
    private String driverPhoneCode;
    private String driverPhoneNumber;
    private boolean vendorReassigned;
    private String assignedAt;
}
