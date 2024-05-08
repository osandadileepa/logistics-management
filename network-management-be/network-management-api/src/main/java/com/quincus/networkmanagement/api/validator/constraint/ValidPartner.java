package com.quincus.networkmanagement.api.validator.constraint;

import com.quincus.networkmanagement.api.validator.PartnerValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PartnerValidator.class)
public @interface ValidPartner {
    String message() default "Invalid Partner/Vendor";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
