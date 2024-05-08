package com.quincus.apigateway.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;
    private String timestamp;
    private String status;
}
