package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConsigneeValidationTest extends ValidationTest {
    @Test
    void consignee_WithMissingFields_ShouldHaveViolations() {
        assertThat(validateModel(new Consignee())).isNotEmpty();
    }

    @Test
    void consignee_WithValidFields_ShouldHaveNoViolations() {
        Consignee consignee = new Consignee();
        consignee.setName("testName");
        consignee.setEmail("test@email.com");
        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");
        assertThat(validateModel(consignee)).isEmpty();
    }

    @Test
    void consignee_WithBlankFields_ShouldHaveViolations() {
        Consignee consignee = new Consignee();
        consignee.setName("   ");
        consignee.setEmail("  ");
        Address address = new Address();
        address.setCountry("  ");
        address.setCity("  ");
        address.setState("    ");
        Set<ConstraintViolation<Object>> violations = validateModel(consignee);
        violations.addAll(validateModel(address));
        assertThat(violations).hasSize(4);
    }

    @Test
    void consignee_WithEmptyFields_ShouldHaveViolations() {
        Consignee consignee = new Consignee();
        consignee.setName("");
        consignee.setEmail("");
        Address address = new Address();
        address.setCountryName("");
        address.setCityName("");
        address.setStateName("");
        Set<ConstraintViolation<Object>> violations = validateModel(consignee);
        violations.addAll(validateModel(address));
        assertThat(violations).hasSize(4);
    }
}
