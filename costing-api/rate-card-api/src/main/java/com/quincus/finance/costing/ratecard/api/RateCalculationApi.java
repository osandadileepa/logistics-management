package com.quincus.finance.costing.ratecard.api;

import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationOutput;

public interface RateCalculationApi {

    RateCalculationOutput calculate(RateCalculationInput rateCalculationInput);

}
