package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.validator.constraint.FieldType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

public class FieldTypeValidator implements ConstraintValidator<FieldType, Object> {
    private Class<?> type;
    private Annotation[] annotations;

    private final DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    };

    @Override
    public void initialize(FieldType constraintAnnotation) {
        type = constraintAnnotation.type();
        annotations = constraintAnnotation.getClass().getAnnotations();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return !isMandatory();
        }

        String stringValue = value.toString();
        if (type != Boolean.class && (isValidBoolean(stringValue) || value instanceof Boolean)) {
            return false;
        }

        if (type == Temporal.class) {
            return isValidTemporalString(stringValue) || value instanceof Temporal;
        }
        if (type == Number.class) {
            return isValidNumber(stringValue) || value instanceof Number;
        }
        if (type == String.class) {
            return value instanceof String;
        }
        return type.isInstance(value);
    }

    private boolean isValidBoolean(String stringValue) {
        return BooleanUtils.TRUE.equalsIgnoreCase(stringValue) || BooleanUtils.FALSE.equalsIgnoreCase(stringValue);
    }

    private boolean isMandatory() {
        for (Annotation annotation : annotations) {
            if (annotation instanceof NotBlank || annotation instanceof NotNull) return true;
        }
        return false;
    }

    private boolean isValidTemporalString(String value) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                formatter.parse(value);
                return true; // Parsing successful, value is a valid Temporal
            } catch (DateTimeParseException e) {
                // Ignore and continue to the next formatter
            }
        }
        return false; // No formatter matched, value is not a valid Temporal
    }

    private boolean isValidNumber(String value) {
        return NumberUtils.isCreatable(value);
    }
}
