package com.quincus.finance.costing.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class CostingApiExceptionHandler {

    private static final String ERR_NOT_IMPLEMENTED = "Not Implement";
    private static final String ERR_INTERNAL_ERROR = "Internal Server Error";
    private static final String ERR_VALIDATION_ERROR = "Validation Error";

    @ExceptionHandler
    public ResponseEntity<CostingApiError> handleCostingApiException(CostingApiException e) {
        log.warn("Costing API exception occurred: {}", e);
        return constructErrorResponse(e.getMessage(), e.getErrors(), e.getErrorCode());
    }

    protected ResponseEntity<CostingApiError> constructErrorResponse(String message, List<String> errors, HttpStatus httpStatus) {
        CostingApiError response = new CostingApiError();
        response.setMessage(message);
        response.setErrors(errors);
        response.setStatus(httpStatus.name());
        response.setId(UUID.randomUUID().toString());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler
    public ResponseEntity<CostingApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.add(error.getField() + " " + error.getDefaultMessage()));
        return constructErrorResponse(ERR_VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CostingApiError> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = new ArrayList<>();
        e.getConstraintViolations()
                .forEach(error -> errors.add(error.getMessage()));
        return constructErrorResponse(ERR_VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<CostingApiError> handleException(Exception e) {
        return constructErrorResponse(ERR_INTERNAL_ERROR, Collections.singletonList(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<CostingApiError> handleNotImplementedException(NotImplementedException e) {
        return constructErrorResponse(ERR_NOT_IMPLEMENTED, Collections.singletonList(e.getMessage()), HttpStatus.NOT_IMPLEMENTED);
    }
}
