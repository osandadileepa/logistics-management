package com.quincus.finance.costing.ratecard.impl.validator;

import com.quincus.finance.costing.ratecard.api.model.RateCard;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

@Component
public class RateCardValidator implements ConstraintValidator<RateCardConstraint, RateCard> {

    private static final String ERR_INVALID_MIN_MAX = "min should not be greater than max";

    @Override
    public boolean isValid(RateCard rateCard, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        List<String> errors = new ArrayList<>();

        if (rateCard.getMin() != null && rateCard.getMax() != null && rateCard.getMin().compareTo(rateCard.getMax()) > 0) {
            errors.add(ERR_INVALID_MIN_MAX);
        }

        if (!errors.isEmpty()) {
            for(String error : errors) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(error).addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}
