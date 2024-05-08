package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.api.validator.constraint.ValidPartner;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PartnerValidator implements ConstraintValidator<ValidPartner, Partner> {
    @Override
    public boolean isValid(Partner partner, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        return hasIdOrName(partner, context);
    }

    private boolean hasIdOrName(Partner partner, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (StringUtils.isAllBlank(partner.getId(), partner.getName())) {
            context.buildConstraintViolationWithTemplate("vendor/partner must have either id or name")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }
}
