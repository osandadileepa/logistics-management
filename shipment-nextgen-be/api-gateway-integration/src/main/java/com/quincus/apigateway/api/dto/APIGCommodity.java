package com.quincus.apigateway.api.dto;

import lombok.Data;

@Data
public class APIGCommodity {
    private Long quantity;
    private String code;
    private String name;
    private String desc;
    private String shCode;
    private String packagingType;
    private String note;
}
