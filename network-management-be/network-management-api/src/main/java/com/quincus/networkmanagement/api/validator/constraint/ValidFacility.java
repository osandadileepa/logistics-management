package com.quincus.networkmanagement.api.validator.constraint;

import com.quincus.networkmanagement.api.validator.FacilityValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FacilityValidator.class)
public @interface ValidFacility {
    String message() default "Invalid Facility";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
