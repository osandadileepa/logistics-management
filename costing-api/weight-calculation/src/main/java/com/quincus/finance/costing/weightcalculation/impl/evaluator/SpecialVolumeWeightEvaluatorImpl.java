package com.quincus.finance.costing.weightcalculation.impl.evaluator;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.common.exception.CostingApiException;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Set;

@Component
@Slf4j
public class SpecialVolumeWeightEvaluatorImpl implements SpecialVolumeWeightEvaluator {

    private static final Set<String> VOLUME_WEIGHT_PLACEHOLDER = Set.of("L", "W", "H");

    @Override
    public BigDecimal calculateVolumeWeight(WeightCalculationInput weightCalculationInput, SpecialVolumeWeightRule specialVolumeWeightRule) {

        BigDecimal customFormulaEvaluation = evaluateCustomFormula(weightCalculationInput, specialVolumeWeightRule.getCustomFormula());

        log.debug("Evaluation of custom formula: {}", customFormulaEvaluation);

        for(Conversion conversion : specialVolumeWeightRule.getConversions()) {
            if(isInRange(customFormulaEvaluation, conversion)) {
                log.debug("Custom formula evaluation falls into a record in conversion table: {}", conversion);
                return conversion.getResult();
            }
        }

        return customFormulaEvaluation;
    }

    private BigDecimal evaluateCustomFormula(WeightCalculationInput weightCalculationInput, String specialVolumeWeightFormula) {
        try {
            Assert.notNull(weightCalculationInput, "Weight Calculation Input must not be null");
            Assert.notNull(specialVolumeWeightFormula, "Special Volume Weight Formula must not be null");

            return BigDecimal.valueOf(new ExpressionBuilder(specialVolumeWeightFormula.toUpperCase())
                    .variables(VOLUME_WEIGHT_PLACEHOLDER)
                    .build()
                    .setVariable("L", weightCalculationInput.getLength().doubleValue())
                    .setVariable("W", weightCalculationInput.getWidth().doubleValue())
                    .setVariable("H", weightCalculationInput.getHeight().doubleValue())
                    .evaluate());
        } catch (Exception exception) {
            log.warn("Error on calculating formula: {} with weight calculation input: {}", specialVolumeWeightFormula, weightCalculationInput);
            throw new CostingApiException(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public boolean isValidFormula(String specialVolumeWeightFormula) {
        try {
            Assert.notNull(specialVolumeWeightFormula, "Special Volume Weight Formula must not be null");
            return new ExpressionBuilder(specialVolumeWeightFormula.toUpperCase())
                    .variables(VOLUME_WEIGHT_PLACEHOLDER)
                    .build()
                    //Need to pass Double.MIN_VALUE or any other values will suffice in order to check the validity of the formula.
                    .setVariable("L", Double.MIN_VALUE)
                    .setVariable("W", Double.MIN_VALUE)
                    .setVariable("H", Double.MIN_VALUE)
                    .validate()
                    .isValid();
        } catch (Exception exception) {
            log.warn("Error on validating formula: {}", specialVolumeWeightFormula);
            return false;
        }
    }

    private boolean isInRange(BigDecimal customFormulaEvaluation, Conversion conversion) {
        return customFormulaEvaluation.compareTo(conversion.getFrom()) >= 0 &&
            customFormulaEvaluation.compareTo(conversion.getTo()) < 0;
    }
}
