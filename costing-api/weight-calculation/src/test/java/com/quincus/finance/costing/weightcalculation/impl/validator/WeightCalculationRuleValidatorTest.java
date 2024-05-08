package com.quincus.finance.costing.weightcalculation.impl.validator;

import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeightCalculationRuleValidatorTest {

    private final WeightCalculationRuleValidator weightCalculationRuleValidator = new WeightCalculationRuleValidator();

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test
    @DisplayName("GIVEN ALWAYS_PICK_VOLUME_WEIGHT and no volumeWeightRule WHEN validate THEN return false")
    void returnFalseWhenAlwaysPickVolumeWeightAndNoVolumeWeightRule() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_VOLUME_WEIGHT);
        rule.setVolumeWeightRule(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT and no volumeWeightRule WHEN validate THEN return false")
    void returnFalseWhenHigherValueBetweenAndNoVolumeWeightRule() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT);
        rule.setVolumeWeightRule(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN ALWAYS_PICK_ACTUAL_WEIGHT and no volumeWeightRule WHEN validate THEN return true")
    void returnTrueWhenHigherValueBetweenAndNoVolumeWeightRule() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT);
        rule.setVolumeWeightRule(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN STANDARD without divisor WHEN validate THEN return false")
    void returnFalseWhenStandardWithoutDivisor() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightRule(VolumeWeightRule.STANDARD);
        rule.setStandardVolumeWeightRuleDivisor(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN STANDARD with divisor WHEN validate THEN return true")
    void returnTrueWhenStandardWithDivisor() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightRule(VolumeWeightRule.STANDARD);
        rule.setStandardVolumeWeightRuleDivisor(BigDecimal.ONE);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN SPECIAL without divisor WHEN validate THEN return true")
    void returnFalseWhenStandardWithDivisor() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightRule(VolumeWeightRule.SPECIAL);
        rule.setStandardVolumeWeightRuleDivisor(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN divisor is zero WHEN validate THEN return false")
    void returnFalseWhenDivisorIsZero() {
        WeightCalculationRule rule = dummyRule();
        rule.setStandardVolumeWeightRuleDivisor(BigDecimal.ZERO);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN actual weight min below max WHEN validate THEN return true")
    void returnTrueWhenActualWeightMinBelowMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setActualWeightMin(BigDecimal.TEN);
        rule.setActualWeightMax(BigDecimal.valueOf(200));
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN actual weight min above max WHEN validate THEN return false")
    void returnFalseWhenActualWeightMinAboveMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setActualWeightMin(BigDecimal.valueOf(200));
        rule.setActualWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN actual weight min only WHEN validate THEN return true")
    void returnTrueWhenActualWeightMinOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setActualWeightMin(BigDecimal.TEN);
        rule.setActualWeightMax(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN actual weight max only WHEN validate THEN return true")
    void returnTrueWhenActualWeightMaxOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setActualWeightMin(null);
        rule.setActualWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN volume weight min below max WHEN validate THEN return true")
    void returnTrueWhenVolumeWeightMinBelowMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightMin(BigDecimal.TEN);
        rule.setVolumeWeightMax(BigDecimal.valueOf(200));
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN volume weight min above max WHEN validate THEN return false")
    void returnFalseWhenVolumeWeightMinAboveMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightMin(BigDecimal.valueOf(200));
        rule.setVolumeWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN volume weight min only WHEN validate THEN return true")
    void returnTrueWhenVolumeWeightMinOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightMin(BigDecimal.TEN);
        rule.setVolumeWeightMax(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN volume weight max only WHEN validate THEN return true")
    void returnTrueWhenVolumeWeightMaxOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightMin(null);
        rule.setVolumeWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN chargeable weight min below max WHEN validate THEN return true")
    void returnTrueWhenChargeableWeightMinBelowMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMin(BigDecimal.TEN);
        rule.setChargeableWeightMax(BigDecimal.valueOf(200));
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN chargeable weight min above max WHEN validate THEN return false")
    void returnFalseWhenChargeableWeightMinAboveMax() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMin(BigDecimal.valueOf(200));
        rule.setChargeableWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN chargeable weight min only WHEN validate THEN return true")
    void returnTrueWhenChargeableWeightMinOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMin(BigDecimal.TEN);
        rule.setChargeableWeightMax(null);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN chargeable weight max only WHEN validate THEN return true")
    void returnTrueWhenChargeableWeightMaxOnly() {
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMin(null);
        rule.setChargeableWeightMax(BigDecimal.TEN);
        assertThat(weightCalculationRuleValidator.isValid(rule, context)).isTrue();
    }

}
