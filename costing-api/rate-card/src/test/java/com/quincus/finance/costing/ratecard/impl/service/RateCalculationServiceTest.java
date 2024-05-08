package com.quincus.finance.costing.ratecard.impl.service;

import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationOutput;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.api.model.RateCardCalculationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.quincus.finance.costing.ratecard.impl.data.RateCalculationTestData.dummyInput;
import static com.quincus.finance.costing.ratecard.impl.data.RateCalculationTestData.dummyRateCard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateCalculationServiceTest {

    @InjectMocks
    private RateCalculationService rateCalculationService;

    @Mock
    private RateCardService rateCardService;

    @Test
    @DisplayName("GIVEN flat calculation type WHEN calculate THEN return expected")
    void returnExpectedWhenCalculateFlat() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(new BigDecimal("500.0"));
    }

    @Test
    @DisplayName("GIVEN per weight unit calculation type WHEN calculate THEN return expected")
    void returnExpectedWhenCalculatePerWeightUnit() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setCalculationType(RateCardCalculationType.PER_WEIGHT_UNIT);
        rateCard.setRateValue(BigDecimal.valueOf(0.23));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(new BigDecimal("7.820"));
    }

    @Test
    @DisplayName("GIVEN per distance unit calculation type WHEN calculate THEN return expected")
    void returnExpectedWhenCalculatePerDistanceUnit() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setCalculationType(RateCardCalculationType.PER_DISTANCE_UNIT);
        rateCard.setRateValue(BigDecimal.valueOf(0.5));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(new BigDecimal("6.00"));
    }

    @Test
    @DisplayName("GIVEN good value percentage calculation type WHEN calculate THEN return expected")
    void returnExpectedWhenCalculateByGoodValuePercentage() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setCalculationType(RateCardCalculationType.PERCENTAGE_OF_GOOD_VALUE);
        rateCard.setRateValue(BigDecimal.valueOf(0.1));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(new BigDecimal("5.60"));
    }

    @Test
    @DisplayName("GIVEN between min and max WHEN calculate THEN return expected")
    void returnExpectedWhenBetweenMinMax() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setMin(BigDecimal.valueOf(450));
        rateCard.setMax(BigDecimal.valueOf(550));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(new BigDecimal("500.0"));
    }

    @Test
    @DisplayName("GIVEN below min rate WHEN calculate THEN return expected")
    void returnExpectedWhenBelowMinRate() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setMin(BigDecimal.valueOf(1200));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(BigDecimal.valueOf(1200));
    }

    @Test
    @DisplayName("GIVEN above max rate WHEN calculate THEN return expected")
    void returnExpectedWhenAboveMaxRate() {
        RateCalculationInput input = dummyInput();
        RateCard rateCard = dummyRateCard();
        rateCard.setMax(BigDecimal.valueOf(450));

        when(rateCardService.get(any())).thenReturn(rateCard);
        RateCalculationOutput output = rateCalculationService.calculate(input);

        assertThat(output.getRateCardId()).isEqualTo(rateCard.getId());
        assertThat(output.getResult()).isEqualTo(BigDecimal.valueOf(450));
    }

}
