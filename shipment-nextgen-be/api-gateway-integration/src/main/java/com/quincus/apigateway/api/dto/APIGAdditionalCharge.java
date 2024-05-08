package com.quincus.apigateway.api.dto;

import lombok.Data;

@Data
public class APIGAdditionalCharge {
    private String chargeCode;
    private String unitType;
    private String currency;
    private String timeUnit;
    private String distanceUnit;
    private String weightUnit;
    private String chargeAmount;
    private String timestamp;
}
