package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTypeValidationTest extends ValidationTest {

    @Test
    void serviceType_WithMissingFields_ShouldHaveViolations() {
        assertThat(validateModel(new ServiceType())).isNotEmpty();
    }

    @Test
    void serviceType_WithValidFields_ShouldHaveNoViolations() {
        ServiceType serviceType = new ServiceType();
        serviceType.setCode("SC1");
        serviceType.setName("Express");
        assertThat(validateModel(serviceType)).isEmpty();
    }

    @Test
    void serviceType_WithBlankFields_ShouldHaveViolations() {
        ServiceType serviceType = new ServiceType();
        serviceType.setCode(" ");
        serviceType.setName(" ");
        Set<ConstraintViolation<Object>> violations = validateModel(serviceType);
        assertThat(violations).hasSize(2);
    }

    @Test
    void serviceType_WithEmptyFields_ShouldHaveViolations() {
        ServiceType serviceType = new ServiceType();
        serviceType.setCode("");
        serviceType.setName("");
        Set<ConstraintViolation<Object>> violations = validateModel(serviceType);
        assertThat(violations).hasSize(2);
    }
}
