package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.networkmanagement.api.validator.constraint.ValidCurrency;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, Currency> {
    @Override
    public boolean isValid(Currency currency, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        return hasIdOrCode(currency, context);
    }

    private boolean hasIdOrCode(Currency currency, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (StringUtils.isAllBlank(currency.getId(), currency.getCode())) {
            context.buildConstraintViolationWithTemplate("currency must have either id or code")
                    .addPropertyNode("code")
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }
}
