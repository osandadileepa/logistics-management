package com.quincus.finance.costing.ratecard.impl.validator;

import com.quincus.finance.costing.ratecard.api.model.RateCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateCardValidatorTest {

    
    private final RateCardValidator rateCardValidator = new RateCardValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);


    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test
    @DisplayName("GIVEN invalid rate card WHEN validate THEN return false")
    void returnFalseWhenRateCardIsInvalid() {
        RateCard rateCard = new RateCard();
        rateCard.setMax(BigDecimal.ZERO);
        rateCard.setMin(BigDecimal.ONE);
        assertThat(rateCardValidator.isValid(rateCard, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN valid rate card WHEN validate THEN return true")
    void returnTrueWhenRateCardIsInvalid() {
        RateCard rateCard = new RateCard();
        rateCard.setMax(BigDecimal.ONE);
        rateCard.setMin(BigDecimal.ZERO);
        assertThat(rateCardValidator.isValid(rateCard, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN valid rate with min WHEN validate THEN return true")
    void returnTrueWhenRateCardHasMin() {
        RateCard rateCard = new RateCard();
        rateCard.setMin(BigDecimal.ONE);
        assertThat(rateCardValidator.isValid(rateCard, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN valid rate with max WHEN validate THEN return true")
    void returnTrueWhenRateCardHasMax() {
        RateCard rateCard = new RateCard();
        rateCard.setMax(BigDecimal.ONE);
        assertThat(rateCardValidator.isValid(rateCard, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN valid rate without min max WHEN validate THEN return true")
    void returnTrueWhenNoMinMax() {
        RateCard rateCard = new RateCard();
        assertThat(rateCardValidator.isValid(rateCard, context)).isTrue();
    }


}
