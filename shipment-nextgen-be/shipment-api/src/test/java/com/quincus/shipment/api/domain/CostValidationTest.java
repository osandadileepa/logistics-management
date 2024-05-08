package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CostValidationTest extends ValidationTest {
    @Test
    @DisplayName("when missing fields then return invalid")
    void returnInvalidWhenMissingFields() {
        assertThat(validateModel(new Cost())).isNotEmpty();
    }

    @Test
    @DisplayName("when provided all required fields then return valid")
    void returnValidWhenAllRequiredFieldsProvided() {
        Cost cost = new Cost();
        CostType costType = new CostType();
        costType.setId(UUID.randomUUID().toString());
        cost.setCostType(costType);
        cost.setIssuedTimezone("UTC 8:00");

        Currency currency = new Currency();
        currency.setId(UUID.randomUUID().toString());
        cost.setCurrency(currency);

        cost.setDriverId(UUID.randomUUID().toString());
        cost.setIssuedDate(LocalDateTime.MIN);
        cost.setOrganizationId(UUID.randomUUID().toString());

        cost.setCostAmount(BigDecimal.TEN);
        cost.setRemarks("This is a short message");

        CostShipment costShipment = new CostShipment();
        costShipment.setId(UUID.randomUUID().toString());
        CostSegment costSegment = new CostSegment();
        costSegment.setSegmentId(UUID.randomUUID().toString());
        costShipment.setSegments(List.of(costSegment));

        cost.setShipments(List.of(costShipment));

        ProofOfCost proof = new ProofOfCost();
        proof.setUrl("dummy_url");
        proof.setFileName("dummy_file.png");
        proof.setFileSize(100L);
        cost.setProofOfCost(List.of(proof));
        assertThat(validateModel(cost)).isEmpty();
    }

    @Test
    @DisplayName("when invalid costAmount decimal places then return invalid")
    void returnInvalidWhenCostAmount() {
        Cost cost = new Cost();

        cost.setCostAmount(BigDecimal.valueOf(123.456));

        Set<String> violationsMessages = validateModel(cost).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertThat(violationsMessages).contains("numeric value out of bounds (<15 digits>.<2 digits> expected)");
    }

    @Test
    @DisplayName("when below minimum costAmount then return invalid")
    void returnInvalidCostAmountBelowMin() {
        Cost cost = new Cost();

        cost.setCostAmount(BigDecimal.valueOf(-1.20));

        Set<String> violationsMessages = validateModel(cost).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertThat(violationsMessages).contains("must be greater than 0.0");
    }

}
