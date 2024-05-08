package com.quincus.finance.costing.common.util;

import com.quincus.finance.costing.common.web.model.RoundingLogic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.quincus.finance.costing.common.util.RoundingLogicUtil.applyRoundingLogic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RoundingLogicUtilTest {

    private static Stream<Arguments> provideValidInputAndFormula() {
        return Stream.of(
                arguments(BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(1000)),
                arguments(BigDecimal.valueOf(1000), BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(1000)),
                arguments(BigDecimal.valueOf(1004), BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(1000)),
                arguments(BigDecimal.valueOf(1004), BigDecimal.valueOf(10), BigDecimal.valueOf(4), BigDecimal.valueOf(1010)),
                arguments(BigDecimal.valueOf(1004), BigDecimal.valueOf(10), BigDecimal.valueOf(3), BigDecimal.valueOf(1010)),
                arguments(BigDecimal.valueOf(125), BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(200)),
                arguments(BigDecimal.valueOf(125), BigDecimal.valueOf(100), BigDecimal.valueOf(30), BigDecimal.valueOf(100)),
                arguments(BigDecimal.valueOf(274427), BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(274000)),
                arguments(BigDecimal.valueOf(274427), BigDecimal.valueOf(1000), BigDecimal.valueOf(400), BigDecimal.valueOf(275000)),
                arguments(BigDecimal.valueOf(0.00231), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.002), BigDecimal.valueOf(0.01)),
                arguments(BigDecimal.valueOf(0.005567), BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0005), BigDecimal.valueOf(0.006)),
                arguments(BigDecimal.valueOf(0.005567), BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0006), BigDecimal.valueOf(0.005)),
                arguments(BigDecimal.valueOf(0.00231), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.005), new BigDecimal("0.00")),
                arguments(BigDecimal.valueOf(0.000567), BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0006), new BigDecimal("0.000"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidInputAndFormula")
    @DisplayName("GIVEN valid formula and input WHEN calculate volume weight THEN calculate")
    void returnExpectedWhenApplyRoundingLogic(
            BigDecimal value,
            BigDecimal roundTo,
            BigDecimal roundingThreshold,
            BigDecimal expectedResult) {

        RoundingLogic roundingLogic = new RoundingLogic(roundTo, roundingThreshold);

        BigDecimal result = applyRoundingLogic(value, roundingLogic);
        assertThat(result).isEqualTo(expectedResult);
    }

}
