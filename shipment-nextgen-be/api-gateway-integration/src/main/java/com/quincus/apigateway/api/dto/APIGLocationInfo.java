package com.quincus.apigateway.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class APIGLocationInfo {
    private String locationId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

