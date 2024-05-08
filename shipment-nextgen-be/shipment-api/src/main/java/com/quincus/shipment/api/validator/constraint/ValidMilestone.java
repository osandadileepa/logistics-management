package com.quincus.shipment.api.validator.constraint;

import com.quincus.shipment.api.validator.MilestoneValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MilestoneValidator.class)
public @interface ValidMilestone {
    String message() default "{error.milestone}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
