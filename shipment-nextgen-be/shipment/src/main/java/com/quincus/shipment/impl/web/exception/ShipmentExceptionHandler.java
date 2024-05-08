package com.quincus.shipment.impl.web.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.exception.AlertNotFoundException;
import com.quincus.shipment.api.exception.CostNotFoundException;
import com.quincus.shipment.api.exception.FlightStatsMessageException;
import com.quincus.shipment.api.exception.InvalidCostException;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.api.exception.InvalidMilestoneException;
import com.quincus.shipment.api.exception.JobNotFoundException;
import com.quincus.shipment.api.exception.LocationHierarchyDuplicateException;
import com.quincus.shipment.api.exception.MilestoneNotFoundException;
import com.quincus.shipment.api.exception.NetworkLaneException;
import com.quincus.shipment.api.exception.NetworkLaneNotFoundException;
import com.quincus.shipment.api.exception.PackageDimensionException;
import com.quincus.shipment.api.exception.PackageJourneySegmentException;
import com.quincus.shipment.api.exception.PartnerNotAllowedException;
import com.quincus.shipment.api.exception.ProofOfCostException;
import com.quincus.shipment.api.exception.QPortalUpsertException;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.SegmentNotFoundException;
import com.quincus.shipment.api.exception.ShipmentInvalidStatusException;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.api.exception.ShipmentJourneyMismatchException;
import com.quincus.shipment.api.exception.ShipmentJourneyNotFoundException;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.api.exception.UpdateOrderAdditionalChargesException;
import com.quincus.shipment.api.exception.UserGroupNotAllowedException;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.exception.model.QuincusFieldError;
import com.quincus.web.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.FAILED_UPDATE_ORDER_ADDITIONAL_CHARGES;
import static com.quincus.shipment.api.constant.ShipmentErrorCode.INTERNAL_PERSISTENCE_ERROR;
import static com.quincus.shipment.api.constant.ShipmentErrorCode.MILESTONE_NOT_FOUND;
import static com.quincus.shipment.api.constant.ShipmentErrorCode.RESOURCE_ACCESS_FORBIDDEN;

@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ShipmentExceptionHandler {

    private static final String ERR_VALIDATION_MSG = "There is a validation error in your request";

    private ResponseEntity<Response<QuincusError>> buildResponse(QuincusError quincusError, HttpStatus httpStatus) {
        Response<QuincusError> response = new Response<>(quincusError);
        response.setMessage(quincusError.message());
        response.setStatus(String.valueOf(httpStatus.value()));
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleShipmentNotFoundException(ShipmentNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SHIPMENT_NOT_FOUND.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleShipmentInvalidStatusException(ShipmentInvalidStatusException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SHIPMENT_INVALID_STATUS.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleAlertNotFoundException(AlertNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.ALERT_NOT_FOUND.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleSegmentException(SegmentException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SEGMENT_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleShipmentJourneyNotFoundException(ShipmentJourneyNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SHIPMENT_JOURNEY_NOT_FOUND.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleShipmentJourneyMismatchException(ShipmentJourneyMismatchException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SHIPMENT_JOURNEY_MISMATCH.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleShipmentJourneyException(ShipmentJourneyException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SHIPMENT_JOURNEY_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleInvalidMilestoneMessageException(InvalidMilestoneException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_MILESTONE.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleSegmentNotFoundException(SegmentNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.SEGMENT_NOT_FOUND.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handlePackageDimensionException(PackageDimensionException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.PACKAGE_DIMENSION_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleInvalidFacilityException(PackageJourneySegmentException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        e.getErrors()
                .forEach(error -> fieldErrors.add(
                        new QuincusFieldError(
                                null,
                                error,
                                null,
                                null
                        )
                ));

        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.VALIDATION_ERROR.name())
                .message(e.getMessage())
                .fieldErrors(fieldErrors);

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleInvalidNetworkLaneException(NetworkLaneException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        e.getErrors()
                .forEach(error -> fieldErrors.add(
                        new QuincusFieldError(
                                null,
                                error,
                                null,
                                null
                        )
                ));

        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.VALIDATION_ERROR.name())
                .message(e.getMessage())
                .fieldErrors(fieldErrors);

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleNetworkLaneNotFoundException(NetworkLaneNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_NETWORK_LANE_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleFlightStatsMessageException(FlightStatsMessageException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.FLIGHT_STATS_MESSAGE_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleJsonParseException(JsonParseException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.JSON_PARSE_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleJsonMappingException(JsonMappingException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.JSON_MAPPING_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleInvalidFormatException(InvalidFormatException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new QuincusFieldError(
                        ShipmentExceptionHelper.getFieldName(e),
                        "has an invalid format",
                        "InvalidFormat",
                        e.getValue()
                )
        );

        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_FORMAT.name())
                .message("There is an invalid format in your request")
                .fieldErrors(fieldErrors);

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleInvalidEnumValueException(InvalidEnumValueException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_ENUM_ERROR.name())
                .message(e.getMessage());

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.add(
                        new QuincusFieldError(
                                ShipmentExceptionHelper.toSnakeCase(error.getField()),
                                error.getDefaultMessage(),
                                error.getCode(),
                                error.getRejectedValue()
                        )
                ));

        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.VALIDATION_ERROR.name())
                .message(ERR_VALIDATION_MSG)
                .fieldErrors(fieldErrors);


        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(
                new QuincusFieldError(
                        e.getName(),
                        String.format("Failed to convert `%s` to `%s`", e.getName(), Optional.ofNullable(e.getRequiredType())
                                .map(Class::getCanonicalName)
                                .orElse("valid value type")),
                        e.getErrorCode(),
                        e.getValue()
                )
        );
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_FORMAT.name())
                .message("There is an invalid format in your request")
                .fieldErrors(fieldErrors);
        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response<QuincusError>> handleConstraintViolationException(ConstraintViolationException e) {
        List<QuincusFieldError> fieldErrors = new ArrayList<>();
        e.getConstraintViolations()
                .forEach(error -> fieldErrors.add(
                        new QuincusFieldError(
                                ShipmentExceptionHelper.toSnakeCase(error.getPropertyPath().toString()),
                                error.getMessage(),
                                error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                                error.getInvalidValue()
                        )
                ));

        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.VALIDATION_ERROR.name())
                .message(ERR_VALIDATION_MSG)
                .fieldErrors(fieldErrors);

        return buildResponse(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleInvalidCostException(InvalidCostException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_COST_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleCostNotFoundException(CostNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_COST_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleProofOfCostException(ProofOfCostException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.INVALID_PROOF_OF_COST_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleQPortalUpsertException(QPortalUpsertException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.QPORTAL_UPSERT_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleLocationHierarchyDuplicateException(LocationHierarchyDuplicateException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.DUPLICATE_LOCATION_HIERARCHY_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleSegmentLocationNotAllowedException(SegmentLocationNotAllowedException e) {
        QuincusError response = new QuincusError()
                .code(e.getErrorCode().name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handlePartnerNotAllowedException(PartnerNotAllowedException e) {
        QuincusError response = new QuincusError()
                .code(e.getErrorCode().name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleJobNotFoundException(JobNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(ShipmentErrorCode.JOB_NOT_FOUND.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleSegmentLocationNotAllowedException(UserGroupNotAllowedException e) {
        QuincusError response = new QuincusError()
                .code(RESOURCE_ACCESS_FORBIDDEN.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleUpdateOrderAdditionalChargesException(UpdateOrderAdditionalChargesException e) {
        QuincusError response = new QuincusError()
                .code(FAILED_UPDATE_ORDER_ADDITIONAL_CHARGES.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleMilestoneNotFoundException(MilestoneNotFoundException e) {
        QuincusError response = new QuincusError()
                .code(MILESTONE_NOT_FOUND.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<QuincusError> handleDbAccessException(DataAccessException e) {
        QuincusError response = new QuincusError()
                .code(INTERNAL_PERSISTENCE_ERROR.name())
                .message(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
