package com.quincus.networkmanagement.api.validator.constraint;

import com.quincus.networkmanagement.api.validator.ShipmentProfileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ShipmentProfileValidator.class)
public @interface ValidShipmentProfile {
    String message() default "Invalid ShipmentProfile";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
