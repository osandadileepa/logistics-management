package com.quincus.apigateway.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiRequest<T> {
    private T data;
}
