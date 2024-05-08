package com.quincus.apigateway.web.model;

import com.quincus.apigateway.api.dto.APIGCustomerInfo;
import com.quincus.apigateway.api.dto.APIGLocationInfo;
import com.quincus.apigateway.api.dto.APIGPackage;
import com.quincus.apigateway.api.dto.APIGPicInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApiGatewayUpdateOrderProgressRequest {
    private String orderNo;
    private String segmentId;
    private boolean isIsFirstSegment;
    private boolean isIsLastSegment;
    private String milestoneTime;
    private String timestamp;
    private BigDecimal orderTotalCodAmt;
    private APIGPicInfo picInfo;
    private APIGCustomerInfo customerInfo;
    private APIGLocationInfo locationInfo;
    private List<APIGPackage> packages;
}
