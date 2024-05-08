package com.quincus.finance.costing.ratecard.impl.service;

import com.quincus.finance.costing.ratecard.api.RateCalculationApi;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationOutput;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class RateCalculationService implements RateCalculationApi {

    private final RateCardService rateCardService;

    @Override
    public RateCalculationOutput calculate(RateCalculationInput input) {

        RateCard rateCard = rateCardService.get(input.getRateCardId());

        RateCalculationOutput output = new RateCalculationOutput();

        output.setRateCardId(rateCard.getId());

        BigDecimal result = calculateByType(input, rateCard);
        result = applyMinMax(result, rateCard.getMin(), rateCard.getMax());

        output.setResult(result);

        return output;
    }

    private BigDecimal calculateByType(RateCalculationInput input, RateCard rateCard) {
        return switch (rateCard.getCalculationType()) {
            case PER_DISTANCE_UNIT -> rateCard.getRateValue().multiply(input.getDistanceValue());
            case PER_WEIGHT_UNIT -> rateCard.getRateValue().multiply(input.getWeightValue());
            case PERCENTAGE_OF_GOOD_VALUE -> rateCard.getRateValue().multiply(input.getGoodValue());
            default -> rateCard.getRateValue();
        };
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


}
