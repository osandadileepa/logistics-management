package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.validator.constraint.ValidFacility;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FacilityValidator implements ConstraintValidator<ValidFacility, Facility> {
    @Override
    public boolean isValid(Facility facility, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        return hasIdOrName(facility, context);
    }

    private boolean hasIdOrName(Facility facility, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (StringUtils.isAllBlank(facility.getId(), facility.getName())) {
            context.buildConstraintViolationWithTemplate("facility must have either id or name")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }
}
