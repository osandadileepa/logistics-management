package com.quincus.ext.annotation;

import com.quincus.ext.annotation.validator.FileNamesValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileNamesValidator.class)
public @interface FileNames {
    String message() default "Invalid filenames";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
