package com.quincus.networkmanagement.impl.web.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.quincus.networkmanagement.api.constant.NetworkManagementErrorCode;
import com.quincus.networkmanagement.api.exception.ConnectionNotFoundException;
import com.quincus.networkmanagement.api.exception.DuplicateConnectionCodeException;
import com.quincus.networkmanagement.api.exception.DuplicateNodeCodeException;
import com.quincus.networkmanagement.api.exception.InvalidConnectionException;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;
import com.quincus.networkmanagement.api.exception.InvalidMmeGraphException;
import com.quincus.networkmanagement.api.exception.InvalidRRuleException;
import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.api.exception.JobCancelationNotAllowedException;
import com.quincus.networkmanagement.api.exception.JobNotFoundException;
import com.quincus.networkmanagement.api.exception.MmeApiCallException;
import com.quincus.networkmanagement.api.exception.NoRecordsToExportException;
import com.quincus.networkmanagement.api.exception.NodeNotFoundException;
import com.quincus.networkmanagement.api.exception.QPortalSyncFailedException;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class NetworkManagementExceptionHandler {
    private static final String ERR_VALIDATION_MSG = "There is a validation error in your request";

    private ResponseEntity<NetworkManagementError> buildResponse(NetworkManagementError shipmentError, HttpStatus httpStatus) {
        return new ResponseEntity<>(shipmentError, httpStatus);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.add(
                        new NetworkManagementFieldError(
                                NetworkManagementExceptionHelper.camelToSnakeCase(error.getField()),
                                error.getDefaultMessage(),
                                error.getCode(),
                                error.getRejectedValue()
                        )
                ));

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.VALIDATION_ERROR)
                .message(ERR_VALIDATION_MSG)
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleConstraintViolationException(ConstraintViolationException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        e.getConstraintViolations()
                .forEach(error -> fieldErrors.add(
                        new NetworkManagementFieldError(
                                error.getPropertyPath().toString(),
                                error.getMessage(),
                                error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                                error.getInvalidValue()
                        )
                ));

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.VALIDATION_ERROR)
                .message(ERR_VALIDATION_MSG)
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidFormatException(InvalidFormatException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        NetworkManagementExceptionHelper.getFieldName(e.getPath()),
                        "Has an invalid format",
                        "InvalidFormat",
                        e.getValue()
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_FORMAT)
                .message("There is an invalid format in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleMismatchedInputException(MismatchedInputException e) {

        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        NetworkManagementExceptionHelper.getFieldName(e.getPath()),
                        "Input mismatched",
                        "MismatchedInput"
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.MISMATCHED_INPUT)
                .message("There is a mismatched input in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleMismatchedMethodArgumentException(MethodArgumentTypeMismatchException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        e.getName(),
                        "Method argument mismatched",
                        "MismatchedMethodArgument"
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.MISMATCHED_METHOD_ARGUMENT)
                .message("There is a mismatched method argument in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleNodeNotFoundException(NodeNotFoundException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.NODE_NOT_FOUND)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidMmeGraphException(InvalidMmeGraphException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_MME_GRAPH)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleMmeApiCallException(MmeApiCallException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(e.getCode())
                .message(e.getMessage());
        return buildResponse(response, e.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleConnectionNotFoundException(ConnectionNotFoundException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.CONNECTION_NOT_FOUND)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleJobNotFoundException(JobNotFoundException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.JOB_NOT_FOUND)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleDuplicateNodeCodeException(DuplicateNodeCodeException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        e.getField(),
                        e.getMessage(),
                        NetworkManagementErrorCode.DUPLICATE_NODE_CODE.toString(),
                        e.getRejectedValue()
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.DUPLICATE_NODE_CODE)
                .message(e.getMessage())
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleJobCancellationNotAllowedException(JobCancelationNotAllowedException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.JOB_CANCELLATION_NOT_ALLOWED)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleQPortalSyncFailedException(QPortalSyncFailedException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.UPSERT_FACILITY_FAILED)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleDuplicateConnectionCodeException(DuplicateConnectionCodeException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        e.getField(),
                        e.getMessage(),
                        NetworkManagementErrorCode.DUPLICATE_CONNECTION_CODE.toString(),
                        e.getRejectedValue()
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.DUPLICATE_CONNECTION_CODE)
                .message(e.getMessage())
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidEnumValueException(InvalidEnumValueException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_FORMAT)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidTemplateException(InvalidTemplateException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_FILE)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidConnectionException(InvalidConnectionException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_CONNECTION)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleNoRecordsToExportException(NoRecordsToExportException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.NO_RECORDS_TO_EXPORT)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleBindException(BindException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.add(
                        new NetworkManagementFieldError(
                                NetworkManagementExceptionHelper.camelToSnakeCase(error.getField()),
                                "is an invalid value",
                                error.getCode(),
                                error.getRejectedValue()
                        )
                ));

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_REQUEST)
                .message("There is an invalid value in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidFieldTypeException(InvalidFieldTypeException e) {
        List<NetworkManagementFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new NetworkManagementFieldError(
                        e.getField(),
                        "is an invalid value",
                        "InvalidValue"
                )
        );

        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_VALUE)
                .message("There is an invalid value in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<NetworkManagementError> handleInvalidRRuleException(InvalidRRuleException e) {
        NetworkManagementError response = new NetworkManagementError()
                .code(NetworkManagementErrorCode.INVALID_RRULE)
                .message(e.getMessage());
        return buildResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
