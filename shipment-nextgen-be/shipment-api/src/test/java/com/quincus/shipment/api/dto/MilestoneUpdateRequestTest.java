package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.MilestoneSource;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class MilestoneUpdateRequestTest {
    private final Validator validator;

    MilestoneUpdateRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidation_SuccessfulValidation() {
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setOrderNumber("12345");
        request.setSegmentId("123");
        request.setVendorId("VENDOR001");
        request.setMilestone("Completed");
        request.setMilestoneTime("2023-01-01T12:00:00Z");
        request.setShipmentIds(List.of("shpId1"));
        request.setSource(MilestoneSource.VENDOR);

        Set<ConstraintViolation<MilestoneUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void testValidation_MissingMandatoryFields() {
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setSegmentId("  ");
        request.setMilestone("Completed");
        Set<ConstraintViolation<MilestoneUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(5);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("must not be blank");
    }

    @Test
    void testValidation_InvalidFieldType() {
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setOrderNumber("true");
        request.setSegmentId("abc");
        request.setVendorId("VENDOR001");
        request.setMilestone("Completed");
        request.setMilestoneTime("2023-01-01T12:00:00Z");
        request.setWaybillNumber("false");
        request.setRecipientName("John Doe");
        request.setDepartmentFloorSuiteComments("Floor 1");
        request.setProofOfDeliveryTime("2023");
        request.setBranchName("Branch 1");
        request.setShipmentIds(List.of("shpId1"));

        Set<ConstraintViolation<MilestoneUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(4);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Invalid field type");
        assertThat(violations)
                .extracting(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactlyInAnyOrder(
                        tuple("orderNumber", "Invalid field type"),
                        tuple("proofOfDeliveryTime", "Invalid field type"),
                        tuple("waybillNumber", "Invalid field type"),
                        tuple("source", "must not be null")
                );
    }
}