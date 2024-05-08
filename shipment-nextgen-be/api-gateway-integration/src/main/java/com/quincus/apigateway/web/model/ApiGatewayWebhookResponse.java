package com.quincus.apigateway.web.model;

import lombok.Data;

@Data
public class ApiGatewayWebhookResponse {
    private String message;
    private String timestamp;
    private String status;
    private Object request;
}
