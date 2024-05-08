package com.quincus.web.common.exception;

import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;
import com.quincus.web.common.exception.model.MissingMandatoryFieldsException;
import com.quincus.web.common.exception.model.ObjectNotFoundException;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Order
@Slf4j
public class CommonExceptionHandler {

    private static final String ERR_ACCESS_DENIED = "Access Denied. The user does not have valid authorization to access this resource.";
    private static final String ERR_INTERNAL_SERVER = "Internal Error: {}";
    private static final String ERR_DATABASE_UNAVAILABLE = "We're experiencing issues processing your request due to a system constraint. Please try again later.";
    private static final String ERR_API_CALL = "API Call Error: {}";
    private static final String ERR_OBJECT_NOT_FOUND = "Object Not Found Error: {}";
    private static final String ERR_UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type Error: {}";
    private static final String ERR_OPERATION_NOT_ALLOWED = "Operation Not Allowed Error: {}";
    private static final String ERR_METHOD_NOT_ALLOWED = "Method Not Allowed Error: {}";
    private static final String ERR_MAXIMUM_UPLOAD_SIZE = "The uploaded file exceeds the maximum allowed size";
    private static final String ERR_EMPTY_ATTACHMENT = "Attachment file is empty";
    private static final String ERR_MISSING_MANDATORY_FIELD = "Missing mandatory field(s): {}";
    private static final String ERR_INVALID_FIELD_TYPE = "Invalid field type: {}";

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.add(error.getField() + " " + error.getDefaultMessage()));
        Response<String> response = new Response<>(null);
        response.setErrors(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleApiCallException(ApiCallException e) {
        log.error(ERR_API_CALL, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, e.getResponseStatus());
    }

    @ExceptionHandler
    public ResponseEntity<Response<Object>> handleObjectNotFoundException(ObjectNotFoundException e) {
        log.warn(ERR_OBJECT_NOT_FOUND, e.getMessage(), e);
        Response<Object> response = new Response<>(e.getObject());
        if (e.getObject() == null) {
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException e) {
        log.warn(ERR_UNSUPPORTED_MEDIA_TYPE, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleOperationNotAllowedException(OperationNotAllowedException e) {
        log.warn(ERR_OPERATION_NOT_ALLOWED, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleGenericException(Exception e) {
        log.error(ERR_INTERNAL_SERVER, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        log.error(ERR_METHOD_NOT_ALLOWED, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler
    public ResponseEntity<Response<Object>> handleQuincusException(QuincusException e) {
        log.error(ERR_INTERNAL_SERVER, e.getMessage(), e);
        Response<Object> response = new Response<>(e.getObject());
        if (e.getObject() == null) {
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleQuincusValidationException(QuincusValidationException e) {
        log.warn(ERR_INTERNAL_SERVER, e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleOrganizationDetailsNotFoundException(OrganizationDetailsNotFoundException e) {
        log.error(ERR_INTERNAL_SERVER, e.getMessage());
        Response<String> response = new Response<>(null);
        response.setMessage(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<String>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(ERR_ACCESS_DENIED + " {}", e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(ERR_ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    @ExceptionHandler
    public ResponseEntity<Response<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn(ERR_MAXIMUM_UPLOAD_SIZE + ": {}", e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(ERR_MAXIMUM_UPLOAD_SIZE);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Response<String>> handleMultipartException(MultipartException e) {
        log.warn(ERR_EMPTY_ATTACHMENT + ": {}", e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(ERR_EMPTY_ATTACHMENT);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingMandatoryFieldsException.class)
    public ResponseEntity<Response<Object>> handleMissingMandatoryFieldsException(MissingMandatoryFieldsException e) {
        log.warn(ERR_MISSING_MANDATORY_FIELD, e.getMessage(), e);
        Response<Object> response = new Response<>(e.getObject());
        if (e.getObject() == null) {
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFieldTypeException.class)
    public ResponseEntity<Response<Object>> handleInvalidFieldTypeException(InvalidFieldTypeException e) {
        log.warn(ERR_INVALID_FIELD_TYPE, e.getMessage(), e);
        Response<Object> response = new Response<>(e.getObject());
        if (e.getObject() == null) {
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Response<String>> handleDataAccessException(DataAccessException e) {
        log.error("Database access error due to `{}`. Please try again later.", e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(ERR_DATABASE_UNAVAILABLE);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<Response<String>> handleJDBCConnectionException(JDBCConnectionException e) {
        log.error("Could not establish a connection with the database due to `{}`. Please try again later.", e.getMessage(), e);
        Response<String> response = new Response<>(null);
        response.setMessage(ERR_DATABASE_UNAVAILABLE);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
