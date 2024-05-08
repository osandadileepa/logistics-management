package com.quincus.apigateway.web.model;

import com.quincus.apigateway.api.dto.APIGAdditionalCharge;
import lombok.Data;

import java.util.List;

@Data
public class ApiGatewayUpdateOrderAdditionalChargesRequest {
    private String orderNo;
    private String segmentId;
    private boolean isIsFirstSegment;
    private boolean isIsLastSegment;
    private String jobType;
    private String orderStatus;
    private List<APIGAdditionalCharge> additionalCharges;
}
