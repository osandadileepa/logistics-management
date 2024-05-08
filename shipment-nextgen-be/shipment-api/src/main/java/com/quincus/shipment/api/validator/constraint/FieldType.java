package com.quincus.shipment.api.validator.constraint;

import com.quincus.shipment.api.validator.FieldTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldTypeValidator.class)
public @interface FieldType {
    String message() default "Invalid field type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<?> type();
}
