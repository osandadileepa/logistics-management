package com.quincus.finance.costing.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class CostingApiException extends RuntimeException {
    private final HttpStatus errorCode;
    private final List<String> errors;
    public CostingApiException(String message, HttpStatus errorCode) {
        super(message);
        this.errors = null;
        this.errorCode = errorCode;
    }

    public CostingApiException(String message, String error, HttpStatus errorCode) {
        super(message);
        this.errors = Collections.singletonList(error);
        this.errorCode = errorCode;
    }

    public CostingApiException(String message, List<String> errors, HttpStatus errorCode) {
        super(message);
        this.errors = errors;
        this.errorCode = errorCode;
    }
}