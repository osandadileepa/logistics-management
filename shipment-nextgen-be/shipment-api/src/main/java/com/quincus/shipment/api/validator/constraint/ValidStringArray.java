package com.quincus.shipment.api.validator.constraint;

import com.quincus.shipment.api.validator.StringArrayValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates string array and elements of string.
 * Have options for array size check, min and max.
 * And string elements for length(size) check, min and max,
 * And string elements for not-null and not-blank validations.
 * <p>
 * Note:
 * Please check default values for all options before defining values, you might not have to define all options.
 *
 * @author vishalkumar.patel
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StringArrayValidator.class)
public @interface ValidStringArray {

    String message();

    boolean notNullEach() default false;

    boolean notBlankEach() default false;

    int minLengthEach() default 0;

    int maxLengthEach() default Integer.MAX_VALUE;

    int minSize() default 0;

    int maxSize() default 10000;

    /**
     * Set this to true if each string element is to be validated as UUID
     */
    boolean uuid() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
