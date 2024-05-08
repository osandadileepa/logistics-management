package com.quincus.web.exception;

import com.quincus.core.impl.exception.ApiCallException;
import com.quincus.core.impl.exception.InvalidFieldTypeException;
import com.quincus.core.impl.exception.MissingMandatoryFieldsException;
import com.quincus.core.impl.exception.OperationNotAllowedException;
import com.quincus.core.impl.exception.OrganizationDetailsNotFoundException;
import com.quincus.core.impl.exception.QuincusException;
import com.quincus.core.impl.exception.QuincusValidationException;
import com.quincus.core.impl.exception.UserDetailsNotFoundException;
import com.quincus.web.model.Response;
import com.quincus.web.model.error.QuincusError;
import com.quincus.web.model.error.QuincusErrorCode;
import com.quincus.web.model.error.QuincusFieldError;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;


@ControllerAdvice
@Order
@Slf4j
public class QuincusExceptionHandler {
    private static final String STRING_PLACE_HOLDER = "`%s`";
    private static final String LOG_PLACE_HOLDER = "{}";
    private static final String MSG_ACCESS_DENIED = "Unauthorized access: This resource requires valid authorization.";
    private static final String MSG_INTERNAL_ERROR = "Unexpected error occurred.";
    private static final String MSG_DATABASE_ISSUES = "Currently experiencing database constraints. Please try again later.";
    private static final String MSG_API_CALL_ERROR = "An error occurred while making the API call";
    private static final String MSG_USER_DETAILS_ERROR = "User Details Error";
    private static final String MSG_UNSUPPORTED_MEDIA_TYPE = "Provided media type is not supported.";
    private static final String MSG_OPERATION_NOT_ALLOWED = "The requested operation is forbidden.";
    private static final String MSG_FILE_SIZE_EXCEEDED = "File size exceeds the maximum limit.";
    private static final String MSG_EMPTY_ATTACHMENT = "File attachment seems to be empty.";
    private static final String MSG_MISSING_MANDATORY_FIELD = "Required field(s) missing in the request";
    private static final String MSG_INVALID_FIELD_TYPE = "Field type is invalid";
    private static final String MSG_VALIDATION_ERRORS = "Request validation failed.";
    private static final String MSG_ORGANIZATION_NOT_FOUND_ERROR = "Couldn't find the organization.";
    private static final String MSG_JDBC_CONNECTION = "Database connection error due to `{}`. Please try again later.";
    private static final String MSG_DATA_ACCESS = "Error occurred while accessing to database due to `{}`. Please try again later.";

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn(MSG_VALIDATION_ERRORS, e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .fieldErrors(e.getFieldErrors().stream()
                        .map(error -> new QuincusFieldError(
                                error.getField(),
                                error.getDefaultMessage(),
                                error.getCode(),
                                error.getRejectedValue()))
                        .toList());
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleApiCallException(ApiCallException e) {
        log.error(MSG_API_CALL_ERROR + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.API_CALL_ERROR.name())
                .errorMessage(MSG_API_CALL_ERROR);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleUserDetailsNotFoundException(UserDetailsNotFoundException e) {
        log.warn(MSG_USER_DETAILS_ERROR + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.USER_DETAILS_ERROR.name())
                .errorMessage(MSG_USER_DETAILS_ERROR);
        return buildResponse(quincusError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException e) {
        log.warn(MSG_UNSUPPORTED_MEDIA_TYPE + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(MSG_UNSUPPORTED_MEDIA_TYPE);
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleOperationNotAllowedException(OperationNotAllowedException e) {
        log.warn(MSG_OPERATION_NOT_ALLOWED + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.OPERATION_NOT_ALLOWED_ERROR.name())
                .errorMessage(MSG_OPERATION_NOT_ALLOWED);
        return buildResponse(quincusError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleGenericException(Exception e) {
        log.error(MSG_INTERNAL_ERROR + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.GENERIC_EXCEPTION_ERROR.name())
                .errorMessage(MSG_INTERNAL_ERROR);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleQuincusException(QuincusException e) {
        log.error(MSG_INTERNAL_ERROR + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.GENERIC_EXCEPTION_ERROR.name())
                .errorMessage(MSG_INTERNAL_ERROR);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleQuincusValidationException(QuincusValidationException e) {
        log.error(MSG_VALIDATION_ERRORS + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(String.format(MSG_VALIDATION_ERRORS + STRING_PLACE_HOLDER, e.getMessage()));
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleOrganizationDetailsNotFoundException(OrganizationDetailsNotFoundException e) {
        log.error(MSG_ORGANIZATION_NOT_FOUND_ERROR + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.ORGANIZATION_DETAILS_NOT_FOUND_ERROR.name())
                .errorMessage(MSG_ORGANIZATION_NOT_FOUND_ERROR);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(MSG_ACCESS_DENIED + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.ACCESS_DENIED_ERROR.name())
                .errorMessage(MSG_ACCESS_DENIED);
        return buildResponse(quincusError, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn(MSG_FILE_SIZE_EXCEEDED + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(MSG_FILE_SIZE_EXCEEDED);
        return buildResponse(quincusError, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleMultipartException(MultipartException e) {
        log.warn(MSG_EMPTY_ATTACHMENT + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(MSG_EMPTY_ATTACHMENT);
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingMandatoryFieldsException.class)
    public ResponseEntity<Response<QuincusError>> handleMissingMandatoryFieldsException(MissingMandatoryFieldsException e) {
        log.warn(MSG_MISSING_MANDATORY_FIELD + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(String.format(MSG_MISSING_MANDATORY_FIELD + STRING_PLACE_HOLDER, e.getMessage()));
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFieldTypeException.class)
    public ResponseEntity<Response<QuincusError>> handleInvalidFieldTypeException(InvalidFieldTypeException e) {
        log.warn(MSG_INVALID_FIELD_TYPE + LOG_PLACE_HOLDER, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.VALIDATION_ERROR.name())
                .errorMessage(String.format(MSG_INVALID_FIELD_TYPE + STRING_PLACE_HOLDER, e.getMessage()));
        return buildResponse(quincusError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleDataAccessException(DataAccessException e) {
        log.error(MSG_DATA_ACCESS, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.DATA_LAYER_ERROR.name())
                .errorMessage(MSG_DATABASE_ISSUES);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleJDBCConnectionException(JDBCConnectionException e) {
        log.error(MSG_JDBC_CONNECTION, e.getMessage(), e);
        QuincusError quincusError = new QuincusError()
                .errorCode(QuincusErrorCode.DATA_LAYER_ERROR.name())
                .errorMessage(MSG_DATABASE_ISSUES);
        return buildResponse(quincusError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Response<QuincusError>> buildResponse(QuincusError quincusError, HttpStatus httpStatus) {
        return new ResponseEntity<>(new Response<>(quincusError), httpStatus);
    }
}
