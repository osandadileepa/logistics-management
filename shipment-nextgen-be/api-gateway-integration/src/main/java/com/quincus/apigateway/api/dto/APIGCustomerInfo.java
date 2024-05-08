package com.quincus.apigateway.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class APIGCustomerInfo {
    private String sender;
    private String consignee;
    private String actualSender;
    private String actualRecipient;
    private String customerComment;
    private BigDecimal customerRating;
}
