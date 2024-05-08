package com.quincus.web.common.exception.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiCallException extends QuincusException {
    private static final String FAIL_RESPONSE_MSG_FMT = "External API call failed. URL: %s. Request Body: %s Response Body: %s";
    private static final String EXCEPTION_MSG_FMT = "Exception occurred while calling External API. URL: %s. Request Body: %s Error Message: %s";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Getter
    private final HttpStatus responseStatus;

    public ApiCallException(String url, HttpEntity<?> request, String errorMessage, Exception e) {
        super(String.format(EXCEPTION_MSG_FMT, url, request.getBody(), errorMessage), e.getMessage());
        this.responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiCallException(String url, HttpEntity<?> request, ResponseEntity<?> response) {
        super(String.format(FAIL_RESPONSE_MSG_FMT, url, toString(request.getBody()), toString(response.getBody())));
        this.responseStatus = response.getStatusCode();
    }

    public ApiCallException(String url, HttpEntity<?> request, String errorMessage, HttpStatus httpStatus, Exception e) {
        super(String.format(EXCEPTION_MSG_FMT, url, request.getBody(), errorMessage), e.getMessage());
        this.responseStatus = httpStatus;
    }

    public ApiCallException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.responseStatus = httpStatus;
    }

    private static String toString(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.format("%s-JsonProcessingException-%s", ApiCallException.class.getName(), e.getMessage());
        }
    }
}
