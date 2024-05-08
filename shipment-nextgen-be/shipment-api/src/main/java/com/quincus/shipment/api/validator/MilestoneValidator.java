package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.validator.constraint.ValidMilestone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.isNull;

public class MilestoneValidator implements ConstraintValidator<ValidMilestone, Milestone> {
    private static final String FIELD_REQUIRED_ERR_MSG = "%s must not be null";

    private static final Map<String, Function<Milestone, Object>> milestoneCommonRequiredFieldsMap = Map.ofEntries(
            Map.entry("user_id", Milestone::getUserId),
            Map.entry("hub_id", Milestone::getHubId),
            Map.entry("from_location_id", Milestone::getFromLocationId),
            Map.entry("from_city_id", Milestone::getFromCityId),
            Map.entry("from_state_id", Milestone::getFromStateId),
            Map.entry("from_country_id", Milestone::getFromCountryId),
            Map.entry("to_location_id", Milestone::getToLocationId),
            Map.entry("to_city_id", Milestone::getToCityId),
            Map.entry("to_state_id", Milestone::getToStateId),
            Map.entry("to_country_id", Milestone::getToCountryId),
            Map.entry("milestone_coordinates", Milestone::getMilestoneCoordinates),
            Map.entry("eta", Milestone::getEta),
            Map.entry("shipment_tracking_id", Milestone::getShipmentTrackingId),
            Map.entry("segment_id", Milestone::getSegmentId),
            Map.entry("service_type", Milestone::getServiceType),
            Map.entry("partner_id", Milestone::getPartnerId),
            Map.entry("job_type", Milestone::getJobType),
            Map.entry("vehicle_id", Milestone::getVehicleId),
            Map.entry("vehicle_type", Milestone::getVehicleType),
            Map.entry("driver_id", Milestone::getDriverId),
            Map.entry("driver_name", Milestone::getDriverName));

    @Override
    public boolean isValid(Milestone milestone, ConstraintValidatorContext constraintValidatorContext) {
        String defaultTemplate = constraintValidatorContext.getDefaultConstraintMessageTemplate();
        constraintValidatorContext.disableDefaultConstraintViolation();

        boolean isValidated = true;

        if (isNull(milestone.getMilestoneTime())) {
            isValidated = false;
            setRequiredErrorMessage(constraintValidatorContext, "milestone_time");
        }

        if (isNull(milestone.getOrganizationId())) {
            isValidated = false;
            setRequiredErrorMessage(constraintValidatorContext, "organisation_id");
        }

        MilestoneCode code = milestone.getMilestoneCode();
        if (isNull(code)) {
            setRequiredErrorMessage(constraintValidatorContext, "code");

            constraintValidatorContext.buildConstraintViolationWithTemplate(defaultTemplate);
            return false;
        }
        switch (code) {
            case DSP_DISPATCH_SCHEDULED,
                    DSP_ON_ROUTE_TO_PICKUP,
                    DSP_PICKUP_FAILED,
                    OM_PICKUP_CANCELED,
                    SHP_ARRIVED_AT_HUB,
                    DSP_DELIVERY_ON_ROUTE,
                    DSP_DELIVERY_FAILED,
                    OM_DELIVERY_CANCELED,
                    DSP_ASSIGNMENT_UPDATED,
                    DSP_ASSIGNMENT_CANCELED,
                    DSP_DRIVER_ARRIVED -> {
                if (!isDspMilestoneContainCommonRequiredFields(milestone, constraintValidatorContext)) {
                    return false;
                }
                constraintValidatorContext.buildConstraintViolationWithTemplate(defaultTemplate);
                return isValidated;
            }
            case DSP_PICKUP_SUCCESSFUL -> {
                isDspMilestoneContainCommonRequiredFields(milestone, constraintValidatorContext);
                if (isNull(milestone.getSenderName())) {
                    setRequiredErrorMessage(constraintValidatorContext, "sender_name");
                    return false;
                }
                constraintValidatorContext.buildConstraintViolationWithTemplate(defaultTemplate);
                return isValidated;
            }
            case DSP_DELIVERY_SUCCESSFUL -> {
                isDspMilestoneContainCommonRequiredFields(milestone, constraintValidatorContext);
                if (isNull(milestone.getReceiverName())) {
                    setRequiredErrorMessage(constraintValidatorContext, "receiver_name");
                    return false;
                }
                constraintValidatorContext.buildConstraintViolationWithTemplate(defaultTemplate);
                return isValidated;
            }
            default -> {
                // No additional validation so it's immediately valid
                constraintValidatorContext.buildConstraintViolationWithTemplate(defaultTemplate);
                return isValidated;
            }
        }
    }

    private boolean isDspMilestoneContainCommonRequiredFields(Milestone milestone,
                                                              ConstraintValidatorContext context) {
        boolean isCommonRequiredFieldsPresent = true;
        for (Map.Entry<String, Function<Milestone, Object>> dspCommonRequiredField
                : milestoneCommonRequiredFieldsMap.entrySet()) {
            if (isNull(dspCommonRequiredField.getValue().apply(milestone))) {
                isCommonRequiredFieldsPresent = false;
                setRequiredErrorMessage(context, dspCommonRequiredField.getKey());
            }
        }
        return isMilestoneCoordinatesValid(milestone, context) && isCommonRequiredFieldsPresent;
    }

    private boolean isMilestoneCoordinatesValid(Milestone milestone,
                                                ConstraintValidatorContext context) {
        Coordinate milestoneCoordinate = milestone.getMilestoneCoordinates();
        if (isNull(milestoneCoordinate)) {
            return false;
        }
        if (isNull(milestoneCoordinate.getLat())) {
            setRequiredErrorMessage(context, "milestone_coordinates.lat");
            return false;
        }
        if (isNull(milestoneCoordinate.getLon())) {
            setRequiredErrorMessage(context, "milestone_coordinates.lon");
            return false;
        }
        return true;
    }

    private void setRequiredErrorMessage(ConstraintValidatorContext context, String fieldName) {
        setConstraintViolationErrorMessage(context, String.format(FIELD_REQUIRED_ERR_MSG, fieldName));
    }

    private void setConstraintViolationErrorMessage(ConstraintValidatorContext context,
                                                    String constraintErrorMessage) {
        context.buildConstraintViolationWithTemplate(constraintErrorMessage).addConstraintViolation();
    }
}
