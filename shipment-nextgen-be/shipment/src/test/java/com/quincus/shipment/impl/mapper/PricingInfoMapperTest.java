package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PricingInfoMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapDomainToEntity_pricingInfoDomain_shouldReturnPricingInfoEntity() {
        PricingInfo domain = new PricingInfo();
        domain.setId("ID1");
        domain.setCurrency("PHP");
        domain.setBaseTariff(new BigDecimal("2.71"));
        domain.setServiceTypeCharge(new BigDecimal("2.72"));
        domain.setSurcharge(new BigDecimal("2.73"));
        domain.setInsuranceCharge(new BigDecimal("2.74"));
        domain.setExtraCareCharge(new BigDecimal("2.75"));
        domain.setDiscount(new BigDecimal("2.76"));
        domain.setTax(new BigDecimal("2.77"));
        domain.setCod(new BigDecimal("2.78"));

        final JsonNode entity = PricingInfoMapper.mapDomainToEntity(domain, objectMapper);

        assertThat(entity.get("id").asText()).withFailMessage("Pricing Info ID mismatch.").isEqualTo(domain.getId());
        assertThat(entity.get("currency").asText()).withFailMessage("Pricing Info Currency mismatch.").isEqualTo(domain.getCurrency());
        assertThat(entity.get("base_tariff").decimalValue()).withFailMessage("Pricing Info Base Tariff mismatch.").isEqualTo(domain.getBaseTariff());
        assertThat(entity.get("service_type_charge").decimalValue()).withFailMessage("Pricing Info Service Type Charge mismatch.").isEqualTo(domain.getServiceTypeCharge());
        assertThat(entity.get("surcharge").decimalValue()).withFailMessage("Pricing Info Surcharge mismatch.").isEqualTo(domain.getSurcharge());
        assertThat(entity.get("insurance_charge").decimalValue()).withFailMessage("Pricing Info Insurance Charge mismatch.").isEqualTo(domain.getInsuranceCharge());
        assertThat(entity.get("extra_care_charge").decimalValue()).withFailMessage("Pricing Info Extra Care Charge mismatch.").isEqualTo(domain.getExtraCareCharge());
        assertThat(entity.get("discount").decimalValue()).withFailMessage("Pricing Info Discount mismatch.").isEqualTo(domain.getDiscount());
        assertThat(entity.get("tax").decimalValue()).withFailMessage("Pricing Info Tax mismatch.").isEqualTo(domain.getTax());
        assertThat(entity.get("cod").decimalValue()).withFailMessage("Pricing Info C.O.D. mismatch.").isEqualTo(domain.getCod());
    }

    @Test
    void mapEntityToDomain_pricingInfoEntity_shouldReturnPricingInfoDomain() {
        ObjectNode entity = objectMapper.createObjectNode();
        entity.put("id", "id-1");
        entity.put("currency", "php");
        entity.put("base_tariff", 10.71);
        entity.put("service_type_charge", 10.72);
        entity.put("surcharge", 10.73);
        entity.put("insurance_charge", 10.74);
        entity.put("extra_care_charge", 10.75);
        entity.put("discount", 10.76);
        entity.put("tax", 10.77);
        entity.put("cod", 10.78);

        final PricingInfo domain = PricingInfoMapper.mapEntityToDomain(entity, objectMapper);

        assertThat(domain.getId()).withFailMessage("Pricing Info ID mismatch.").isEqualTo(entity.get("id").asText());
        assertThat(domain.getCurrency()).withFailMessage("Pricing Info Currency mismatch.").isEqualTo(entity.get("currency").asText());
        assertThat(domain.getBaseTariff()).withFailMessage("Pricing Info Base Tariff mismatch.").isEqualTo(entity.get("base_tariff").decimalValue());
        assertThat(domain.getServiceTypeCharge()).withFailMessage("Pricing Info Service Type Charge mismatch.").isEqualTo(entity.get("service_type_charge").decimalValue());
        assertThat(domain.getSurcharge()).withFailMessage("Pricing Info Surcharge mismatch.").isEqualTo(entity.get("surcharge").decimalValue());
        assertThat(domain.getInsuranceCharge()).withFailMessage("Pricing Info Insurance Charge mismatch.").isEqualTo(entity.get("insurance_charge").decimalValue());
        assertThat(domain.getExtraCareCharge()).withFailMessage("Pricing Info Extra Care Charge mismatch.").isEqualTo(entity.get("extra_care_charge").decimalValue());
        assertThat(domain.getDiscount()).withFailMessage("Pricing Info Discount mismatch.").isEqualTo(entity.get("discount").decimalValue());
        assertThat(domain.getTax()).withFailMessage("Pricing Info Tax mismatch.").isEqualTo(entity.get("tax").decimalValue());
        assertThat(domain.getCod()).withFailMessage("Pricing Info C.O.D. mismatch.").isEqualTo(entity.get("cod").decimalValue());
    }
}
