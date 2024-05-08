package com.quincus.ext.annotation.validator;

import com.quincus.ext.annotation.UUID;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UUIDValidator implements ConstraintValidator<UUID, String> {

    private boolean required;

    @Override
    public void initialize(UUID constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (required && StringUtils.isBlank(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Must be required and a valid UUID v4 format")
                    .addConstraintViolation();
            return false;
        }
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return isUUID(value);
    }

    private boolean isUUID(String value) {

        try {
            java.util.UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}