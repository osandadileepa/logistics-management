package com.quincus.networkmanagement.impl.web.exception;

import lombok.Getter;

@Getter
public class NetworkManagementFieldError {
    private final String field;
    private final String message;
    private final String code;
    private String rejectedValue;

    public NetworkManagementFieldError(String field, String message, String code, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        if (rejectedValue != null) {
            this.rejectedValue = rejectedValue.toString();
        }
    }

    public NetworkManagementFieldError(String field, String message, String code) {
        this.field = field;
        this.message = message;
        this.code = code;
    }
}
