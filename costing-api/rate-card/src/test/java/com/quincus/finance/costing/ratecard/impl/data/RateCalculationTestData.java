package com.quincus.finance.costing.ratecard.impl.data;

import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.api.model.RateCardCalculationType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class RateCalculationTestData {

    public static RateCalculationInput dummyInput() {
        RateCalculationInput input = new RateCalculationInput();
        input.setDistanceValue(BigDecimal.valueOf(12.0));
        input.setWeightValue(BigDecimal.valueOf(34.0));
        input.setGoodValue(BigDecimal.valueOf(56.0));
        return input;
    }

    public static RateCard dummyRateCard() {
        RateCard rateCard = new RateCard();
        rateCard.setCalculationType(RateCardCalculationType.FLAT);
        rateCard.setRateValue(BigDecimal.valueOf(500.0));
        return rateCard;
    }

}
