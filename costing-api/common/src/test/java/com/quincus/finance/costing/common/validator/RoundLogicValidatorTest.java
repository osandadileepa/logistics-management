package com.quincus.finance.costing.common.validator;

import com.quincus.finance.costing.common.web.model.RoundingLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoundLogicValidatorTest {

    private final RoundingLogicValidator roundingLogicValidator = new RoundingLogicValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test
    @DisplayName("GIVEN null rounding logic WHEN validate THEN return true")
    void returnTrueWhenNull() {
        assertThat(roundingLogicValidator.isValid(null, context)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideValidRoundingLogic")
    @DisplayName("GIVEN valid rounding logic WHEN validate THEN return true")
    void returnTrueWhenValidRoundingLogic(BigDecimal roundTo, BigDecimal threshold) {
        RoundingLogic roundingLogic = new RoundingLogic(roundTo, threshold);
        assertThat(roundingLogicValidator.isValid(roundingLogic, context)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRoundingLogic")
    @DisplayName("GIVEN invalid rounding logic WHEN validate THEN return false")
    void returnFalseWhenInvalidRoundingLogic(BigDecimal roundTo, BigDecimal threshold) {
        RoundingLogic roundingLogic = new RoundingLogic(roundTo, threshold);
        assertThat(roundingLogicValidator.isValid(roundingLogic, context)).isFalse();
    }

    private static Stream<Arguments> provideValidRoundingLogic() {
        return Stream.of(
                arguments(BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0001)),
                arguments(BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0005)),
                arguments(BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0009)),
                arguments(BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.001)),
                arguments(BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.005)),
                arguments(BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.009)),
                arguments(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.01)),
                arguments(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.05)),
                arguments(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.09)),
                arguments(BigDecimal.valueOf(1), BigDecimal.valueOf(0.1)),
                arguments(BigDecimal.valueOf(1), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(1), BigDecimal.valueOf(0.9)),
                arguments(BigDecimal.valueOf(10), BigDecimal.valueOf(1)),
                arguments(BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                arguments(BigDecimal.valueOf(10), BigDecimal.valueOf(9)),
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(10)),
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(50)),
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(90)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(100)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(500)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(900))
        );
    }

    private static Stream<Arguments> provideInvalidRoundingLogic() {
        return Stream.of(
                arguments(BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.005)),
                arguments(BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.0005)),
                arguments(BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.05)),
                arguments(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.005)),
                arguments(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(1), BigDecimal.valueOf(0.05)),
                arguments(BigDecimal.valueOf(1), BigDecimal.valueOf(5)),
                arguments(BigDecimal.valueOf(10), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(10), BigDecimal.valueOf(50)),
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(5)),
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(500)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(50)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(5000)),
                arguments(BigDecimal.valueOf(0.002), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(0.03), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(5), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(60), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(700), BigDecimal.valueOf(0.5)),
                arguments(BigDecimal.valueOf(8000), BigDecimal.valueOf(0.5))
        );
    }

}
