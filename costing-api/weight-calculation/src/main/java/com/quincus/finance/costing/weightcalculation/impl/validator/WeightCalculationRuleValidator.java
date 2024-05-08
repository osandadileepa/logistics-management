package com.quincus.finance.costing.weightcalculation.impl.validator;


import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeightCalculationRuleValidator implements ConstraintValidator<WeightCalculationRuleConstraint, WeightCalculationRule> {

    private static final String ERR_UNDEFINED_VOLUME_WEIGHT_RULE = "volumeWeightRule must be set due to given chargeableWeightRule";
    private static final String ERR_UNDEFINED_DIVISOR = "standardVolumeWeightRuleDivisor is required when volumeWeightRule is set to STANDARD";
    private static final String ERR_DIVISOR_IS_ZERO = "standardVolumeWeightRuleDivisor should not be set to 0";
    private static final String ERR_INVALID_ACTUAL_WEIGHT_MIN_MAX = "actualWeightMin should not be greater than actualWeightMax";
    private static final String ERR_INVALID_VOLUME_WEIGHT_MIN_MAX = "volumeWeightMin should not be greater than volumeWeightMax";
    private static final String ERR_INVALID_CHARGEABLE_WEIGHT_MIN_MAX = "chargeableWeightMin should not be greater than chargeableWeightMax";

    @Override
    public boolean isValid(WeightCalculationRule rule, ConstraintValidatorContext constraintValidatorContext) {

        constraintValidatorContext.disableDefaultConstraintViolation();
        List<String> errors = new ArrayList<>();

        if (!ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT.equals(rule.getChargeableWeightRule()) && rule.getVolumeWeightRule() == null) {
            errors.add(ERR_UNDEFINED_VOLUME_WEIGHT_RULE);
        }

        if (VolumeWeightRule.STANDARD.equals(rule.getVolumeWeightRule())
                && rule.getStandardVolumeWeightRuleDivisor() == null) {
            errors.add(ERR_UNDEFINED_DIVISOR);
        }

        if (BigDecimal.ZERO.equals(rule.getStandardVolumeWeightRuleDivisor())) {
            errors.add(ERR_DIVISOR_IS_ZERO);
        }

        if (rule.getActualWeightMin() != null && rule.getActualWeightMax() != null && rule.getActualWeightMin().compareTo(rule.getActualWeightMax()) > 0) {
            errors.add(ERR_INVALID_ACTUAL_WEIGHT_MIN_MAX);
        }

        if (rule.getVolumeWeightMin() != null && rule.getVolumeWeightMax() != null && rule.getVolumeWeightMin().compareTo(rule.getVolumeWeightMax()) > 0) {
            errors.add(ERR_INVALID_VOLUME_WEIGHT_MIN_MAX);
        }

        if (rule.getChargeableWeightMin() != null && rule.getChargeableWeightMax() != null && rule.getChargeableWeightMin().compareTo(rule.getChargeableWeightMax()) > 0) {
            errors.add(ERR_INVALID_CHARGEABLE_WEIGHT_MIN_MAX);
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
