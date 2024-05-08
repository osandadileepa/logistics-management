package com.quincus.networkmanagement.api.validator.constraint;

import com.quincus.networkmanagement.api.validator.ConnectionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConnectionValidator.class)
public @interface ValidConnection {
    String message() default "Invalid Connection";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
