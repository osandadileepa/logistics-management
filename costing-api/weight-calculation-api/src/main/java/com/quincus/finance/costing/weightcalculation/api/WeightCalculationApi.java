package com.quincus.finance.costing.weightcalculation.api;

import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationOutput;

public interface WeightCalculationApi {

    WeightCalculationOutput calculate(WeightCalculationInput input);

}
