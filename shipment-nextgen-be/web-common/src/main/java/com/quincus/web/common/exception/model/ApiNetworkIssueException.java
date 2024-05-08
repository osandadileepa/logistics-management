package com.quincus.web.common.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiNetworkIssueException extends QuincusException {

    @Getter
    private final HttpStatus responseStatus;

    public ApiNetworkIssueException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.responseStatus = httpStatus;
    }
}
