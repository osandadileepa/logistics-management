package com.quincus.finance.costing.weightcalculation.impl.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SpecialVolumeWeightTemplateValidator.class)
@Target( {ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialVolumeWeightTemplateConstraint {
    String message() default "invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
