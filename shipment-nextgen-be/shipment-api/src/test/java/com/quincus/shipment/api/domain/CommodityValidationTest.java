package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CommodityValidationTest extends ValidationTest {

    @Test
    void commodity_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new Commodity())).isNotEmpty();
    }

    @Test
    void commodity_withValidFields_shouldHaveNoViolations() {
        Commodity commodity = new Commodity();
        commodity.setName("Electronics");
        commodity.setQuantity(1L);
        commodity.setValue(new BigDecimal("100.20"));
        assertThat(validateModel(commodity)).isEmpty();
    }
}
