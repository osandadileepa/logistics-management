package com.quincus.networkmanagement.api.validator.constraint;

import com.quincus.networkmanagement.api.validator.OperatingHoursValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OperatingHoursValidator.class)
public @interface ValidOperatingHours {
    String message() default "Invalid OperatingHours";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}