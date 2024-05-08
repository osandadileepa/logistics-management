package com.quincus.networkmanagement.impl.preprocessor.helper;

import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformDimension;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformVolume;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformWeight;
import static org.assertj.core.api.Assertions.assertThat;

class PreProcessingHelperTest {

    private static Stream<Arguments> provideDimensions() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, DimensionUnit.METERS, "0.0000"),
                Arguments.of(BigDecimal.ZERO, DimensionUnit.FEET, "0.0000"),
                Arguments.of(BigDecimal.ZERO, DimensionUnit.MILLIMETERS, "0.0000"),
                Arguments.of(BigDecimal.ONE, DimensionUnit.METERS, "1.0000"),
                Arguments.of(BigDecimal.ONE, DimensionUnit.FEET, "0.3048"),
                Arguments.of(BigDecimal.ONE, DimensionUnit.MILLIMETERS, "0.0010"),
                Arguments.of(BigDecimal.valueOf(123.45), DimensionUnit.METERS, "123.4500"),
                Arguments.of(BigDecimal.valueOf(123.45), DimensionUnit.FEET, "37.6275"),
                Arguments.of(BigDecimal.valueOf(123.45), DimensionUnit.MILLIMETERS, "0.1234")
        );
    }

    private static Stream<Arguments> provideVolumes() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, VolumeUnit.CUBIC_METERS, "0.0000"),
                Arguments.of(BigDecimal.ZERO, VolumeUnit.CUBIC_FEET, "0.0000"),
                Arguments.of(BigDecimal.ONE, VolumeUnit.CUBIC_METERS, "1.0000"),
                Arguments.of(BigDecimal.ONE, VolumeUnit.CUBIC_FEET, "0.0283"),
                Arguments.of(BigDecimal.valueOf(123.45), VolumeUnit.CUBIC_METERS, "123.4500"),
                Arguments.of(BigDecimal.valueOf(123.45), VolumeUnit.CUBIC_FEET, "3.4936")
        );
    }

    private static Stream<Arguments> provideWeights() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, WeightUnit.KILOGRAMS, "0.0000"),
                Arguments.of(BigDecimal.ZERO, WeightUnit.POUNDS, "0.0000"),
                Arguments.of(BigDecimal.ZERO, WeightUnit.OUNCE, "0.0000"),
                Arguments.of(BigDecimal.ZERO, WeightUnit.GRAMS, "0.0000"),
                Arguments.of(BigDecimal.ONE, WeightUnit.KILOGRAMS, "1.0000"),
                Arguments.of(BigDecimal.ONE, WeightUnit.POUNDS, "0.4536"),
                Arguments.of(BigDecimal.ONE, WeightUnit.OUNCE, "0.0283"),
                Arguments.of(BigDecimal.ONE, WeightUnit.GRAMS, "0.0010"),
                Arguments.of(BigDecimal.valueOf(123.45), WeightUnit.KILOGRAMS, "123.4500"),
                Arguments.of(BigDecimal.valueOf(123.45), WeightUnit.POUNDS, "55.9969"),
                Arguments.of(BigDecimal.valueOf(123.45), WeightUnit.OUNCE, "3.4936"),
                Arguments.of(BigDecimal.valueOf(123.45), WeightUnit.GRAMS, "0.1234")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDimensions")
    @DisplayName("GIVEN uniform dimension value WHEN validate THEN return expected")
    void returnUniformDimensionUnit(BigDecimal value, DimensionUnit unit, String expected) {
        assertThat(toUniformDimension(value, unit)).hasToString(expected);
    }

    @ParameterizedTest
    @MethodSource("provideVolumes")
    @DisplayName("GIVEN uniform dimension value WHEN validate THEN return expected")
    void returnUniformDimensionUnit(BigDecimal value, VolumeUnit unit, String expected) {
        assertThat(toUniformVolume(value, unit)).hasToString(expected);
    }

    @ParameterizedTest
    @MethodSource("provideWeights")
    @DisplayName("GIVEN uniform dimension value WHEN validate THEN return expected")
    void returnUniformDimensionUnit(BigDecimal value, WeightUnit unit, String expected) {
        assertThat(toUniformWeight(value, unit)).hasToString(expected);
    }
}
