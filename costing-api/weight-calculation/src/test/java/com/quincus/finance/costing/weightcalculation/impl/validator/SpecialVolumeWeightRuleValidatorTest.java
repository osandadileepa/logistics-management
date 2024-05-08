package com.quincus.finance.costing.weightcalculation.impl.validator;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.impl.evaluator.SpecialVolumeWeightEvaluator;
import com.quincus.finance.costing.common.exception.CostingApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummySpecialVolumeWeightRule;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidSpecialVolumeWeightRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialVolumeWeightRuleValidatorTest {

    @InjectMocks
    private SpecialVolumeWeightRuleValidator specialVolumeWeightRuleValidator;

    @Mock
    private SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator;

    @ParameterizedTest
    @MethodSource("provideValidConversionTable")
    @DisplayName("GIVEN valid formula and conversion table WHEN validate specialVolumeWeightRule THEN do not throw")
    void shouldNotThrowExceptionWhenValidConversionTable(SpecialVolumeWeightRule rule) {

        when(specialVolumeWeightEvaluator.isValidFormula(any())).thenReturn(true);

        assertDoesNotThrow(() -> specialVolumeWeightRuleValidator.validate(rule));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConversionTable")
    @DisplayName("GIVEN invalid conversion table WHEN validate specialVolumeWeightRule THEN throw exception")
    void shouldThrowExceptionWhenInvalidConversionTable(SpecialVolumeWeightRule rule) {

        when(specialVolumeWeightEvaluator.isValidFormula(any())).thenReturn(true);

        assertThatThrownBy(() -> specialVolumeWeightRuleValidator.validate(rule))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> assertThat(exception.getErrors()).contains("There is a problem with the conversion table specified in the template"));
    }

    @Test
    @DisplayName("GIVEN invalid formula WHEN validate specialVolumeWeightRule THEN throw exception")
    void shouldThrowExceptionWhenInvalidFormula() {
        when(specialVolumeWeightEvaluator.isValidFormula(any())).thenReturn(false);

        assertThatThrownBy(() -> specialVolumeWeightRuleValidator.validate(dummyValidSpecialVolumeWeightRule()))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> assertThat(exception.getErrors()).contains("The custom formula specified in the template is invalid"));
    }

    private static Stream<Arguments> provideValidConversionTable() {
        return Stream.of(
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(15))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(11), BigDecimal.valueOf(20), BigDecimal.valueOf(15))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of()
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(8)),
                                        new Conversion(BigDecimal.valueOf(20), BigDecimal.valueOf(30), BigDecimal.valueOf(10)),
                                        new Conversion(BigDecimal.valueOf(30), BigDecimal.valueOf(40), BigDecimal.valueOf(12)),
                                        new Conversion(BigDecimal.valueOf(50), BigDecimal.valueOf(55), BigDecimal.valueOf(22)),
                                        new Conversion(BigDecimal.valueOf(90), BigDecimal.valueOf(120), BigDecimal.valueOf(30)),
                                        new Conversion(BigDecimal.valueOf(120), BigDecimal.valueOf(9999), BigDecimal.valueOf(50))
                                )
                        )
                )
        );
    }

    private static Stream<Arguments> provideInvalidConversionTable() {
        return Stream.of(
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(20), BigDecimal.valueOf(10), BigDecimal.valueOf(5))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.valueOf(5))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(5), BigDecimal.valueOf(15), BigDecimal.valueOf(5))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5))
                                )
                        )
                ),
                arguments(
                        dummySpecialVolumeWeightRule(
                                List.of(
                                        new Conversion(BigDecimal.valueOf(0), BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                                        new Conversion(BigDecimal.valueOf(20), BigDecimal.valueOf(30), BigDecimal.valueOf(20)),
                                        new Conversion(BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(10))
                                )
                        )
                )
        );
    }

}
