package com.quincus.finance.costing.weightcalculation.impl.evaluator;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.common.exception.CostingApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyInput;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummySpecialVolumeWeightRule;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidSpecialVolumeWeightRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SpecialVolumeWeightEvaluatorTest {
    private final SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator = new SpecialVolumeWeightEvaluatorImpl();

    private static Stream<Arguments> provideValidInputAndFormula() {
        return Stream.of(
                arguments(dummyInput(), "l+w+h", BigDecimal.valueOf(9.0)),
                arguments(dummyInput(), "l-w-h", BigDecimal.valueOf(-5.0)),
                arguments(dummyInput(), "l/w/h", BigDecimal.valueOf(0.16666666666666666)),
                arguments(dummyInput(), "l*w*h", BigDecimal.valueOf(24.0)),
                arguments(dummyInput(), "l/w/H", BigDecimal.valueOf(0.16666666666666666)),
                arguments(dummyInput(), "L*W*h", BigDecimal.valueOf(24.0)),
                arguments(dummyInput(), "l*2", BigDecimal.valueOf(4.0)),
                arguments(dummyInput(), "w*H+3", BigDecimal.valueOf(15.0)),
                arguments(dummyInput(), "w*w*w", BigDecimal.valueOf(27.0)),
                arguments(dummyInput(), "h++++h", BigDecimal.valueOf(8.0))
        );
    }

    private static Stream<Arguments> provideValidFormula() {
        return Stream.of(
                arguments("L+W+H"),
                arguments("l-w-h"),
                arguments("l/w/h"),
                arguments("l*w*h"),
                arguments("l/w/H"),
                arguments("L*W*h"),
                arguments("l*2")
        );
    }

    private static Stream<Arguments> provideInvalidFormula() {
        return Stream.of(
                arguments("aaa"),
                arguments("bbb"),
                arguments("ccc"),
                arguments("dddd")
        );
    }

    private static Stream<Arguments> provideInvalidInputAndFormula() {
        return Stream.of(
                arguments(null, "l+w+h", "Weight Calculation Input must not be null"),
                arguments(dummyInput(), null, "Special Volume Weight Formula must not be null"),
                arguments(dummyInput(), "x+x+v", "Unknown function or variable 'X' at pos 0 in expression 'X+X+V'"),
                arguments(dummyInput(), "a+a+v", "Unknown function or variable 'A' at pos 0 in expression 'A+A+V'")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidInputAndFormula")
    @DisplayName("GIVEN valid formula and input WHEN calculate volume weight THEN calculate")
    void shouldCalculate(WeightCalculationInput weightCalculationInput, String specialVolumeWeightFormula, BigDecimal expectedVolumeWeight) {
        BigDecimal calculatedVolumeWeight = specialVolumeWeightEvaluator.calculateVolumeWeight(weightCalculationInput, dummySpecialVolumeWeightRule(specialVolumeWeightFormula));
        assertThat(calculatedVolumeWeight).isEqualTo(expectedVolumeWeight);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidInputAndFormula")
    @DisplayName("GIVEN invalid formula and input WHEN calculate volume weight THEN throw exception")
    void shouldThrowExceptionWhenCalculate(WeightCalculationInput weightCalculationInput, String specialVolumeWeightFormula, String errorMessage) {
        assertThatThrownBy(() -> specialVolumeWeightEvaluator.calculateVolumeWeight(weightCalculationInput, dummySpecialVolumeWeightRule(specialVolumeWeightFormula)))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                    assertThat(exception.getMessage()).contains(errorMessage);
                });
    }

    @ParameterizedTest
    @MethodSource("provideValidFormula")
    @DisplayName("GIVEN valid formula and input WHEN validate THEN return true")
    void shouldReturnTrueWhenValidate(String specialVolumeWeightFormula) {
        assertThat(specialVolumeWeightEvaluator.isValidFormula(specialVolumeWeightFormula)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFormula")
    @DisplayName("GIVEN invalid formula WHEN validate THEN return false")
    void shouldReturnFalseWhenValidate(String specialVolumeWeightFormula) {
        assertThat(specialVolumeWeightEvaluator.isValidFormula(specialVolumeWeightFormula)).isFalse();
    }

    @Test
    @DisplayName("GIVEN evaluation is in conversion table WHEN calculate volume weight THEN return expected")
    void returnExpectedWhenEvaluationInConversionTable() {
        BigDecimal calculatedVolumeWeight = specialVolumeWeightEvaluator.calculateVolumeWeight(dummyInput(), dummyValidSpecialVolumeWeightRule());
        assertThat(calculatedVolumeWeight).isEqualTo(BigDecimal.valueOf(5.0));
    }

    @Test
    @DisplayName("GIVEN evaluation is not in conversion table WHEN calculate volume weight THEN return expected")
    void returnExpectedWhenEvaluationNotInConversionTable() {
        SpecialVolumeWeightRule rule = dummySpecialVolumeWeightRule(List.of(
            new Conversion(BigDecimal.valueOf(90.0), BigDecimal.valueOf(120.0), BigDecimal.valueOf(30.0))
        ));

        BigDecimal calculatedVolumeWeight = specialVolumeWeightEvaluator.calculateVolumeWeight(dummyInput(), rule);
        assertThat(calculatedVolumeWeight).isEqualTo(BigDecimal.valueOf(9.0));
    }
}