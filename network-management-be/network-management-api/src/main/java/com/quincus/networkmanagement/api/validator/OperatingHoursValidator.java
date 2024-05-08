package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.OperatingHours;
import com.quincus.networkmanagement.api.validator.constraint.ValidOperatingHours;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalTime;

public class OperatingHoursValidator implements ConstraintValidator<ValidOperatingHours, OperatingHours> {
    @Override
    public boolean isValid(OperatingHours operatingHours, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        return isValidOperatingHours(operatingHours, context);
    }

    private boolean isValidOperatingHours(OperatingHours operatingHours, ConstraintValidatorContext context) {
        return hasAtLeastOneOperatingHours(operatingHours, context) &&
                isValidStartAndEndTime(operatingHours.getMonStartTime(), operatingHours.getMonEndTime(), operatingHours.getMonProcessingTime(), context, "mon") &&
                isValidStartAndEndTime(operatingHours.getTueStartTime(), operatingHours.getTueEndTime(), operatingHours.getTueProcessingTime(), context, "tue") &&
                isValidStartAndEndTime(operatingHours.getWedStartTime(), operatingHours.getWedEndTime(), operatingHours.getWedProcessingTime(), context, "wed") &&
                isValidStartAndEndTime(operatingHours.getThuStartTime(), operatingHours.getThuEndTime(), operatingHours.getThuProcessingTime(), context, "thu") &&
                isValidStartAndEndTime(operatingHours.getFriStartTime(), operatingHours.getFriEndTime(), operatingHours.getFriProcessingTime(), context, "fri") &&
                isValidStartAndEndTime(operatingHours.getSatStartTime(), operatingHours.getSatEndTime(), operatingHours.getSatProcessingTime(), context, "sat") &&
                isValidStartAndEndTime(operatingHours.getSunStartTime(), operatingHours.getSunEndTime(), operatingHours.getSunProcessingTime(), context, "sun");
    }

    private boolean hasAtLeastOneOperatingHours(OperatingHours operatingHours, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (ObjectUtils.allNull(
                operatingHours.getMonStartTime(),
                operatingHours.getTueStartTime(),
                operatingHours.getWedStartTime(),
                operatingHours.getThuStartTime(),
                operatingHours.getFriStartTime(),
                operatingHours.getSatStartTime(),
                operatingHours.getSunStartTime()
        )) {
            context.buildConstraintViolationWithTemplate("node must have at least one operating hours").addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidStartAndEndTime(LocalTime startTime, LocalTime endTime, Integer processingTime, ConstraintValidatorContext context, String prefix) {

        boolean isValid = true;

        if (startTime != null && endTime == null) {
            context.buildConstraintViolationWithTemplate(String.format("%s end time is required when %s start time is provided", prefix, prefix))
                    .addPropertyNode(String.format("%sEndTime", prefix))
                    .addConstraintViolation();
            isValid = false;
        }

        if (endTime != null && startTime == null) {
            context.buildConstraintViolationWithTemplate(String.format("%s start time is required when %s end time is provided", prefix, prefix))
                    .addPropertyNode(String.format("%sStartTime", prefix))
                    .addConstraintViolation();
            isValid = false;
        }

        if (endTime != null && startTime != null && processingTime == null) {
            context.buildConstraintViolationWithTemplate(String.format("%s processing time is required when %s start time and %s end time are provided", prefix, prefix, prefix))
                    .addPropertyNode(String.format("%sProcessingTime", prefix))
                    .addConstraintViolation();
            isValid = false;
        }

        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            context.buildConstraintViolationWithTemplate(String.format("%s end time must be after %s start time", prefix, prefix))
                    .addPropertyNode(String.format("%sEndTime", prefix))
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;

    }

}
