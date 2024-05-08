package com.quincus.shipment.api.validator.constraint;

import com.quincus.shipment.api.validator.CostCategoryValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CostCategoryValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCostCategory {
    String message() default "Currency id is required for for non-time-based cost types category";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
