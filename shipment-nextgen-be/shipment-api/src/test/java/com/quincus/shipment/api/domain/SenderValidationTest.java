package com.quincus.shipment.api.domain;


import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SenderValidationTest extends ValidationTest {

    @Test
    void sender_WithMissingFields_ShouldHaveViolations() {
        assertThat(validateModel(new Sender())).isNotEmpty();
    }

    @Test
    void sender_WithValidFields_ShouldHaveNoViolations() {
        Sender sender = new Sender();
        sender.setName("testName");
        sender.setEmail("test@email.com");
        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");
        assertThat(validateModel(sender)).isEmpty();
    }

    @Test
    void sender_WithBlankFields_ShouldHaveViolations() {
        Sender sender = new Sender();
        sender.setName("   ");
        sender.setEmail("  ");
        Address address = new Address();
        address.setCountry("  ");
        address.setCity("  ");
        address.setState("    ");
        Set<ConstraintViolation<Object>> violations = validateModel(sender);
        violations.addAll(validateModel(address));
        assertThat(violations).hasSize(4);
    }

    @Test
    void sender_WithEmptyFields_ShouldHaveViolations() {
        Sender sender = new Sender();
        sender.setName("");
        sender.setEmail("");
        Address address = new Address();
        address.setCountryName("");
        address.setCityName("");
        address.setStateName("");
        Set<ConstraintViolation<Object>> violations = validateModel(sender);
        violations.addAll(validateModel(address));
        assertThat(violations).hasSize(4);
    }
}
