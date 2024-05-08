package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingInfoValidationTest extends ValidationTest {

    @Test
    void pricingInfo_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new PricingInfo())).isNotEmpty();
    }

    @Test
    void pricingInfo_withValidFields_shouldHaveNoViolations() {
        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("USD");
        pricingInfo.setBaseTariff(new BigDecimal("0.99"));
        pricingInfo.setServiceTypeCharge(new BigDecimal("2.99"));
        pricingInfo.setSurcharge(new BigDecimal("1.99"));
        pricingInfo.setInsuranceCharge(new BigDecimal("0.49"));
        pricingInfo.setExtraCareCharge(new BigDecimal("0.59"));
        pricingInfo.setDiscount(new BigDecimal("1.89"));
        pricingInfo.setTax(new BigDecimal("100.39"));
        pricingInfo.setCod(new BigDecimal("96.59"));
        pricingInfo.setTotal(new BigDecimal("96.59"));
        assertThat(validateModel(pricingInfo)).isEmpty();
    }

    @Test
    void pricingInfo_withLessThanMinimumFields_shouldHaveViolations() {
        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("USD");
        pricingInfo.setBaseTariff(new BigDecimal("-1.01"));
        pricingInfo.setServiceTypeCharge(new BigDecimal("-1.02"));
        pricingInfo.setSurcharge(new BigDecimal("-1.03"));
        pricingInfo.setInsuranceCharge(new BigDecimal("-1.04"));
        pricingInfo.setExtraCareCharge(new BigDecimal("-1.05"));
        pricingInfo.setDiscount(new BigDecimal("-1.06"));
        pricingInfo.setTax(new BigDecimal("-1.07"));
        pricingInfo.setCod(new BigDecimal("-1.08"));
        pricingInfo.setTotal(new BigDecimal("-1.08"));
        assertThat(validateModel(new PricingInfo())).hasSize(9);
    }
}
