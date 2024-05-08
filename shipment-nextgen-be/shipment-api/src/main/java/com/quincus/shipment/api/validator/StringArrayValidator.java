package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.validator.constraint.ValidStringArray;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class StringArrayValidator implements ConstraintValidator<ValidStringArray, String[]> {

    private ValidStringArray constraintAnnotation;

    @Override
    public void initialize(ValidStringArray constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String[] strings, ConstraintValidatorContext constraintValidatorContext) {
        return strings == null ||
                (this.isValidMaxSize(strings) && this.isValidMinSize(strings) &&
                        this.isNotNullEachValid(strings) && this.isNotBlankEachValid(strings) &&
                        this.isMaxLengthEachValid(strings) && this.isMinLengthEachValid(strings) &&
                        this.isUUIDValid(strings));
    }

    private boolean isValidMaxSize(final String[] value) {
        return this.constraintAnnotation.maxSize() >= value.length;
    }

    private boolean isValidMinSize(final String[] value) {
        return this.constraintAnnotation.minSize() <= value.length;
    }

    private boolean isNotNullEachValid(final String[] value) {
        if (this.constraintAnnotation.notNullEach()) {
            return Arrays.stream(value).allMatch(Objects::nonNull);
        } else {
            return true;
        }
    }

    private boolean isNotBlankEachValid(final String[] value) {
        if (this.constraintAnnotation.notBlankEach()) {
            return Arrays.stream(value).allMatch(StringUtils::isNotBlank);
        } else {
            return true;
        }
    }

    private boolean isMaxLengthEachValid(final String[] value) {
        final Predicate<String> predicate = a -> a.length() <= this.constraintAnnotation.maxLengthEach();
        return Arrays.stream(value).allMatch(predicate);
    }

    private boolean isMinLengthEachValid(final String[] value) {
        final Predicate<String> predicate = a -> a.length() >= this.constraintAnnotation.minLengthEach();
        return Arrays.stream(value).allMatch(predicate);
    }

    private boolean isUUIDValid(final String[] values) {
        if (this.constraintAnnotation.uuid()) {
            return Arrays.stream(values).allMatch(this::isValidUUIDValue);
        } else {
            return true;
        }
    }

    private boolean isValidUUIDValue(final String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }
}
