package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.validator.constraint.ValidCostCategory;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class CostCategoryValidator implements ConstraintValidator<ValidCostCategory, Cost> {

    @Override
    public boolean isValid(Cost cost, ConstraintValidatorContext context) {
        return isValidCostCategory(cost, context);
    }

    private boolean isValidCostCategory(Cost cost, ConstraintValidatorContext context) {
        if (isTimeBasedCostTypeCategory(cost)) {
            return true;
        }

        String currencyId = getCurrencyId(cost);

        if (StringUtils.isBlank(currencyId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Must be required and a valid UUID v4 format for non-time-based cost types")
                    .addPropertyNode("currency.id")
                    .addConstraintViolation();
            return false;
        }
        // We don't check UUID format here, leaving that to the UUIDValidator
        return true;
    }

    private String getCurrencyId(Cost cost) {
        return Optional.ofNullable(cost.getCurrency())
                .map(Currency::getId)
                .orElse(null);
    }

    private boolean isTimeBasedCostTypeCategory(Cost cost) {
        return Optional.ofNullable(cost.getCostType())
                .map(costType -> costType.getCategory() == CostCategory.TIME_BASED)
                .orElse(false);
    }
}