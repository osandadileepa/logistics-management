package com.quincus.finance.costing.weightcalculation.impl.evaluator;

import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;

import java.math.BigDecimal;

public interface SpecialVolumeWeightEvaluator {
    
    BigDecimal calculateVolumeWeight(WeightCalculationInput input, SpecialVolumeWeightRule specialVolumeWeightRule);

    boolean isValidFormula(String specialVolumeWeightFormula);
}
