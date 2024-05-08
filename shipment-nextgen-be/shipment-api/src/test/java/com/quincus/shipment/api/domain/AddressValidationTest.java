package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressValidationTest extends ValidationTest {
    @Test
    void address_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new Address())).isNotEmpty();
    }

    @Test
    void address_withValidFields_shouldHaveNoViolations() {
        Address address = new Address();
        address.setCountryName("Country");
        address.setCityName("City");
        address.setStateName("State");
        assertThat(validateModel(address)).isEmpty();
    }

    @Test
    void address_withBlankFields_shouldHaveViolations() {
        Address address = new Address();
        address.setCountryName(" ");
        address.setCityName(" ");
        address.setStateName(" ");
        assertThat(validateModel(address)).isNotEmpty();
    }

    @Test
    void address_withEmptyFields_shouldHaveViolations() {
        Address address = new Address();
        address.setCountryName("");
        address.setCityName("");
        address.setStateName("");
        assertThat(validateModel(address)).isNotEmpty();
    }
}
