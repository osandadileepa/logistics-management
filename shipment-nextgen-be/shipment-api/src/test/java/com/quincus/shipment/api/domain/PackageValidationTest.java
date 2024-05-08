package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.MeasurementUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class PackageValidationTest extends ValidationTest {

    private PackageDimension createDimensionWithAllFields() {
        PackageDimension dimension = new PackageDimension();
        dimension.setMeasurementUnit(MeasurementUnit.METRIC);
        dimension.setLength(new BigDecimal("1.10"));
        dimension.setWidth(new BigDecimal("2.20"));
        dimension.setHeight(new BigDecimal("3.30"));
        dimension.setVolumeWeight(new BigDecimal("4.40"));
        dimension.setGrossWeight(new BigDecimal("5.50"));
        dimension.setChargeableWeight(new BigDecimal("6.60"));
        dimension.setCustom(true);
        return dimension;
    }

    private Commodity createCommodityWithAllFields() {
        Commodity commodity = new Commodity();
        commodity.setName("Toys");
        commodity.setQuantity(3L);
        commodity.setValue(new BigDecimal("7.77"));
        commodity.setDescription("DESC");
        commodity.setHsCode("T01");
        commodity.setCode("CODE");
        commodity.setNote("NOTE");
        commodity.setPackagingType("TYPE");
        return commodity;
    }

    private Commodity createCommodityWithRequiredFieldsOnly() {
        Commodity commodity = new Commodity();
        commodity.setName("Toys");
        commodity.setQuantity(3L);
        return commodity;
    }

    private PricingInfo createPricingInfoWithAllFields() {
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
        pricingInfo.setTotal(new BigDecimal("1.1"));
        return pricingInfo;
    }

    private PricingInfo createPricingInfoWithRequiredFieldsOnly() {
        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("USD");
        pricingInfo.setBaseTariff(new BigDecimal("0.99"));
        pricingInfo.setServiceTypeCharge(new BigDecimal("2.99"));
        pricingInfo.setSurcharge(new BigDecimal("1.99"));
        pricingInfo.setInsuranceCharge(new BigDecimal("0.49"));
        pricingInfo.setExtraCareCharge(new BigDecimal("0.59"));
        pricingInfo.setDiscount(new BigDecimal("1.89"));
        pricingInfo.setTax(new BigDecimal("100.39"));
        pricingInfo.setTotal(new BigDecimal("1.1"));
        return pricingInfo;
    }

    @Test
    void package_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new Package())).isNotEmpty();
    }

    @Test
    void package_withValidFields_shouldHaveNoViolations() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType("SMALL-BOX");
        shipmentPackage.setCurrency("USD");
        shipmentPackage.setTotalValue(new BigDecimal("100.50"));
        shipmentPackage.setDimension(createDimensionWithAllFields());
        shipmentPackage.setCommodities(List.of(createCommodityWithAllFields()));
        shipmentPackage.setPricingInfo(createPricingInfoWithAllFields());
        shipmentPackage.setCode("1000");
        shipmentPackage.setTotalItemsCount(1L);
        assertThat(validateModel(shipmentPackage)).isEmpty();
    }

    @Test
    void package_withRequiredFields_shouldHaveNoViolations() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType("SMALL-BOX");
        shipmentPackage.setCurrency("USD");
        shipmentPackage.setDimension(createDimensionWithAllFields());
        shipmentPackage.setCommodities(List.of(createCommodityWithRequiredFieldsOnly()));
        shipmentPackage.setPricingInfo(createPricingInfoWithRequiredFieldsOnly());
        shipmentPackage.setCode("1000");
        shipmentPackage.setTotalItemsCount(1L);
        assertThat(validateModel(shipmentPackage)).isEmpty();
    }

    @Test
    void package_shouldReturnViolationsWhenCommodityNoteAndDescriptionExceedMaxLength() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType("SMALL-BOX");
        shipmentPackage.setCurrency("USD");
        shipmentPackage.setDimension(createDimensionWithAllFields());
        Commodity commodity = createCommodityWithRequiredFieldsOnly();
        commodity.setNote(generateStringWithLength(100_000));
        commodity.setDescription(generateStringWithLength(100_000));
        shipmentPackage.setCommodities(List.of(commodity));
        shipmentPackage.setPricingInfo(createPricingInfoWithRequiredFieldsOnly());
        shipmentPackage.setCode("1000");
        shipmentPackage.setTotalItemsCount(1L);
        assertThat(validateModel(shipmentPackage)).hasSize(2);
    }

    @Test
    void package_withBlankFields_shouldHaveViolations() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType(" ");
        shipmentPackage.setCurrency(" ");
        shipmentPackage.setTotalValue(new BigDecimal("100.50"));
        shipmentPackage.setDimension(createDimensionWithAllFields());
        shipmentPackage.setCommodities(List.of(createCommodityWithRequiredFieldsOnly()));
        shipmentPackage.setPricingInfo(createPricingInfoWithRequiredFieldsOnly());
        shipmentPackage.setTotalItemsCount(1L);
        assertThat(validateModel(shipmentPackage)).isNotEmpty();
    }

    @Test
    void package_withEmptyFields_shouldHaveViolations() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType("");
        shipmentPackage.setCurrency("");
        shipmentPackage.setTotalValue(new BigDecimal("100.50"));
        shipmentPackage.setDimension(createDimensionWithAllFields());
        shipmentPackage.setCommodities(List.of(createCommodityWithRequiredFieldsOnly()));
        shipmentPackage.setPricingInfo(createPricingInfoWithRequiredFieldsOnly());
        shipmentPackage.setTotalItemsCount(1L);
        assertThat(validateModel(shipmentPackage)).isNotEmpty();
    }

    @Test
    void package_withLessThanMinimumFields_shouldHaveViolations() {
        Package shipmentPackage = new Package();
        shipmentPackage.setType("SMALL-BOX");
        shipmentPackage.setCurrency("USD");
        shipmentPackage.setTotalValue(new BigDecimal("-0.01"));
        shipmentPackage.setDimension(createDimensionWithAllFields());
        shipmentPackage.setCommodities(List.of(createCommodityWithRequiredFieldsOnly()));
        shipmentPackage.setPricingInfo(createPricingInfoWithRequiredFieldsOnly());
        shipmentPackage.setTotalItemsCount(0L);
        assertThat(validateModel(shipmentPackage)).isNotEmpty();
    }

    private static String generateStringWithLength(int length) {
        return "a".repeat(length);
    }
}
