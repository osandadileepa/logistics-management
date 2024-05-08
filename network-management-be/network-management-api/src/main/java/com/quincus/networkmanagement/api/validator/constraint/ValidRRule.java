package com.quincus.networkmanagement.api.validator.constraint;


import com.quincus.networkmanagement.api.validator.RRuleValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RRuleValidator.class)
public @interface ValidRRule {
    String message() default "must be a valid RRULE format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
