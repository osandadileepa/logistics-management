package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.api.helper.EnumUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


class PackageDimensionValidationTest extends ValidationTest {

    @Test
    void packageDimension_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new PackageDimension())).isNotEmpty();
    }

    @Test
    void packageDimension_withValidFields_shouldHaveNoViolations() {
        PackageDimension dimension = new PackageDimension();
        dimension.setMeasurementUnit(EnumUtil.toEnum(MeasurementUnit.class, "metric"));
        dimension.setLength(new BigDecimal("1.10"));
        dimension.setWidth(new BigDecimal("2.20"));
        dimension.setHeight(new BigDecimal("3.30"));
        dimension.setVolumeWeight(new BigDecimal("4.40"));
        dimension.setGrossWeight(new BigDecimal("5.50"));
        dimension.setChargeableWeight(new BigDecimal("6.60"));
        assertThat(validateModel(dimension)).isEmpty();
    }

    @Test
    void packageDimension_withBlankFields_shouldHaveViolations() {
        PackageDimension dimension = new PackageDimension();
        assertThatThrownBy(() -> EnumUtil.toEnum(MeasurementUnit.class, ""))
                .isInstanceOf(InvalidEnumValueException.class);
        dimension.setMeasurementUnit(null);
        dimension.setLength(new BigDecimal("1.10"));
        dimension.setWidth(new BigDecimal("2.20"));
        dimension.setHeight(new BigDecimal("3.30"));
        dimension.setVolumeWeight(new BigDecimal("4.40"));
        dimension.setGrossWeight(new BigDecimal("5.50"));
        dimension.setChargeableWeight(new BigDecimal("6.60"));
        assertThat(validateModel(dimension)).isNotEmpty();
    }

    @Test
    void packageDimension_withEmptyFields_shouldHaveViolations() {
        PackageDimension dimension = new PackageDimension();
        assertThatThrownBy(() -> EnumUtil.toEnum(MeasurementUnit.class, ""))
                .isInstanceOf(InvalidEnumValueException.class);
        dimension.setMeasurementUnit(null);
        dimension.setLength(new BigDecimal("1.10"));
        dimension.setWidth(new BigDecimal("2.20"));
        dimension.setHeight(new BigDecimal("3.30"));
        dimension.setVolumeWeight(new BigDecimal("4.40"));
        dimension.setGrossWeight(new BigDecimal("5.50"));
        dimension.setChargeableWeight(new BigDecimal("6.60"));
        assertThat(validateModel(dimension)).isNotEmpty();
    }

    @Test
    void packageDimension_withLessThanMinimumFields_shouldHaveViolations() {
        PackageDimension dimension = new PackageDimension();
        dimension.setMeasurementUnit(EnumUtil.toEnum(MeasurementUnit.class, "metric"));
        dimension.setLength(new BigDecimal("-0.10"));
        dimension.setWidth(new BigDecimal("-0.20"));
        dimension.setHeight(new BigDecimal("-0.30"));
        dimension.setVolumeWeight(new BigDecimal("-0.40"));
        dimension.setGrossWeight(new BigDecimal("-0.50"));
        dimension.setChargeableWeight(new BigDecimal("-0.60"));
        assertThat(validateModel(dimension)).hasSize(6);
    }
}
