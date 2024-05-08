package com.quincus.finance.costing.weightcalculation.impl.service;

import com.quincus.finance.costing.common.web.model.RoundingLogic;
import com.quincus.finance.costing.weightcalculation.api.WeightCalculationApi;
import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationOutput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.impl.evaluator.SpecialVolumeWeightEvaluator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.quincus.finance.costing.common.util.RoundingLogicUtil.applyRoundingLogic;

@Service
@Slf4j
@AllArgsConstructor
public class WeightCalculationService implements WeightCalculationApi {

    public static final int STANDARD_CALCULATE_VOLUME_WEIGHT_SCALE = 4;
    private final WeightCalculationRuleService weightCalculationRuleService;
    private final SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator;

    public WeightCalculationOutput calculate(WeightCalculationInput input) {

        log.debug("Weight Calculation Input:{} ", input);

        WeightCalculationRule rule = weightCalculationRuleService.getRuleToApply(input.getRuleId(), input.getOrganizationId(), input.getPartnerId());

        WeightCalculationOutput output = new WeightCalculationOutput();
        output.setRuleApplied(rule.getName());
        output.setRuleId(rule.getId());

        calculateChargeableWeight(input, output, rule);

        return output;
    }

    private void calculateChargeableWeight(WeightCalculationInput input, WeightCalculationOutput output, WeightCalculationRule rule) {

        log.debug("Calculating Chargeable Weight using: {}", rule.getChargeableWeightRule());

        BigDecimal chargeableWeight;

        BigDecimal actualWeight = applyMinMax(input.getActualWeight(), rule.getActualWeightMin(), rule.getActualWeightMax());

        log.debug("Actual Weight is: {}", actualWeight);
        output.setActualWeight(actualWeight);

        if (ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT.equals(rule.getChargeableWeightRule())) {
            chargeableWeight = actualWeight;
        } else {

            BigDecimal volumeWeight = calculateVolumeWeight(input, rule);

            volumeWeight = applyMinMax(volumeWeight, rule.getVolumeWeightMin(), rule.getVolumeWeightMax());

            log.debug("Volume Weight is: {}", volumeWeight);
            output.setVolumeWeight(volumeWeight);

            if (ChargeableWeightRule.ALWAYS_PICK_VOLUME_WEIGHT.equals(rule.getChargeableWeightRule())) {
                chargeableWeight = volumeWeight;
            } else {
                chargeableWeight = volumeWeight.max(actualWeight);
            }
        }

        chargeableWeight = applyMinMax(chargeableWeight, rule.getChargeableWeightMin(), rule.getChargeableWeightMax());

        log.debug("Chargeable Weight is: {}", chargeableWeight);
        output.setChargeableWeight(chargeableWeight);

        applyAllRoundingLogic(rule, output);
    }

    private BigDecimal calculateVolumeWeight(WeightCalculationInput input, WeightCalculationRule rule) {

        log.debug("Calculating Volume Weight using: {}", rule.getVolumeWeightRule());

        if (VolumeWeightRule.STANDARD.equals(rule.getVolumeWeightRule())) {
            return input.getLength()
                    .multiply(input.getWidth())
                    .multiply(input.getHeight())
                    .divide(rule.getStandardVolumeWeightRuleDivisor(), STANDARD_CALCULATE_VOLUME_WEIGHT_SCALE, RoundingMode.CEILING);
        } else {
            return specialVolumeWeightEvaluator.calculateVolumeWeight(input, rule.getSpecialVolumeWeightRule());
        }
    }

    private BigDecimal applyMinMax(BigDecimal value, BigDecimal min, BigDecimal max) {

        if (min != null) {
            value = value.max(min);
        }
        if (max != null) {
            value = value.min(max);
        }

        return value;
    }

    private void applyAllRoundingLogic(WeightCalculationRule rule, WeightCalculationOutput output) {

        RoundingLogic roundingLogic = rule.getRoundingLogic();

        log.debug("Applying rounding logic. roundingLogic: {}", roundingLogic);

        if (rule.isActualWeightRounding()) {
            output.setActualWeight(
                    applyRoundingLogic(output.getActualWeight(), roundingLogic)
            );
            log.debug("Rounding logic applied to actual weight. Rounded is value: {} ", output.getActualWeight());
        }

        if (output.getVolumeWeight() != null && rule.isVolumeWeightRounding()) {
            output.setVolumeWeight(
                    applyRoundingLogic(output.getVolumeWeight(), roundingLogic)
            );
            log.debug("Rounding logic applied to volume weight. Rounded is value: {} ", output.getVolumeWeight());
        }

        if (rule.isChargeableWeightRounding()) {
            output.setChargeableWeight(
                    applyRoundingLogic(output.getChargeableWeight(), roundingLogic)
            );
            log.debug("Rounding logic applied to chargeable weight. Rounded is value: {} ", output.getChargeableWeight());
        }

    }
}
