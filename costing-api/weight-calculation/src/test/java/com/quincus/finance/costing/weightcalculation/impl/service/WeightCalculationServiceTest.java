package com.quincus.finance.costing.weightcalculation.impl.service;

import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationOutput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.impl.evaluator.SpecialVolumeWeightEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule.ALWAYS_PICK_VOLUME_WEIGHT;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyInput;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyRule;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidSpecialVolumeWeightRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightCalculationServiceTest {
    @InjectMocks
    private WeightCalculationService weightCalculationService;

    @Mock
    private WeightCalculationRuleService weightCalculationRuleService;

    @Mock
    private SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator;

    @Test
    @DisplayName("GIVEN input and rule WHEN calculate THEN return expected")
    void returnExpectedWhenStandardCalculation() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(new BigDecimal("12.0000"));
        assertThat(output.getRuleApplied()).isEqualTo(rule.getName());
        assertThat(output.getRuleId()).isEqualTo(rule.getId());
    }

    @Test
    @DisplayName("GIVEN always get volume weight WHEN calculate THEN return expected")
    void returnExpectedWhenAlwaysGetVolumeWeight() {
        WeightCalculationInput input = dummyInput();
        input.setActualWeight(new BigDecimal(999));
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ALWAYS_PICK_VOLUME_WEIGHT);

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);

        WeightCalculationOutput output = weightCalculationService.calculate(input);
        assertThat(output.getChargeableWeight()).isEqualTo(new BigDecimal("12.0000"));
    }

    @Test
    @DisplayName("GIVEN always get actual weight WHEN calculate THEN return expected")
    void returnExpectedWhenAlwaysGetActualWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT);

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(input.getActualWeight());
    }

    @Test
    @DisplayName("GIVEN rule with min volume weight WHEN calculate THEN return expected")
    void returnExpectedWhenMinVolumeWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ALWAYS_PICK_VOLUME_WEIGHT);
        rule.setVolumeWeightMin(new BigDecimal(20));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);

        WeightCalculationOutput output = weightCalculationService.calculate(input);
        assertThat(output.getChargeableWeight()).isEqualTo(rule.getVolumeWeightMin());
    }

    @Test
    @DisplayName("GIVEN rule with max volume weight WHEN calculate THEN return expected")
    void returnExpectedWhenMaxVolumeWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ALWAYS_PICK_VOLUME_WEIGHT);
        rule.setVolumeWeightMax(new BigDecimal(5));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);

        WeightCalculationOutput output = weightCalculationService.calculate(input);
        assertThat(output.getChargeableWeight()).isEqualTo(rule.getVolumeWeightMax());
    }

    @Test
    @DisplayName("GIVEN rule with min actual weight WHEN calculate THEN return expected")
    void returnExpectedWhenMinActualWeight() {
        WeightCalculationInput input = dummyInput();
        input.setActualWeight(new BigDecimal(5));
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT);
        rule.setActualWeightMin(new BigDecimal(20));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);

        WeightCalculationOutput output = weightCalculationService.calculate(input);
        assertThat(output.getChargeableWeight()).isEqualTo(rule.getActualWeightMin());
    }

    @Test
    @DisplayName("GIVEN rule with max actual weight WHEN calculate THEN return expected")
    void returnExpectedWhenMaxActualWeight() {
        WeightCalculationInput input = dummyInput();
        input.setActualWeight(new BigDecimal(20));
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_ACTUAL_WEIGHT);
        rule.setActualWeightMax(new BigDecimal(5));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(rule.getActualWeightMax());
    }

    @Test
    @DisplayName("GIVEN rule with min chargeable weight WHEN calculate THEN return expected")
    void returnExpectedWhenMinChargeableWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMin(new BigDecimal(20));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(rule.getChargeableWeightMin());
    }

    @Test
    @DisplayName("GIVEN rule with max chargeable weight WHEN calculate THEN return expected")
    void returnExpectedWhenMaxChargeableWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setChargeableWeightMax(new BigDecimal(10));

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(rule.getChargeableWeightMax());
    }

    @Test
    @DisplayName("GIVEN rule with special weight WHEN calculate THEN return expected")
    void returnExpectedWhenCalculateSpecialVolumeWeight() {
        WeightCalculationInput input = dummyInput();
        WeightCalculationRule rule = dummyRule();
        rule.setSpecialVolumeWeightRule(dummyValidSpecialVolumeWeightRule());
        rule.setVolumeWeightRule(VolumeWeightRule.SPECIAL);
        rule.setChargeableWeightRule(ChargeableWeightRule.ALWAYS_PICK_VOLUME_WEIGHT);

        when(weightCalculationRuleService.getRuleToApply(any(), any(), any())).thenReturn(rule);
        when(specialVolumeWeightEvaluator.calculateVolumeWeight(input, rule.getSpecialVolumeWeightRule())).thenReturn(BigDecimal.valueOf(4));
        WeightCalculationOutput output = weightCalculationService.calculate(input);

        assertThat(output.getChargeableWeight()).isEqualTo(BigDecimal.valueOf(4));
    }

}
