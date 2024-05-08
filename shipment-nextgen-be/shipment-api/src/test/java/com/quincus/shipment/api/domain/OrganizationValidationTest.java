package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class OrganizationValidationTest extends ValidationTest {

    @Test
    void organization_withValidFields_shouldHaveNoViolations() {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID().toString());

        assertThat(validateModel(organization)).isEmpty();
    }

    @Test
    void organization_withMissingMandatoryFields_shouldHaveViolations() {
        Organization organization = new Organization();

        assertThat(validateModel(organization)).isNotEmpty();
    }

    @Test
    void organization_withBlankFields_shouldHaveViolations() {
        Organization organization = new Organization();
        organization.setId(" ");

        assertThat(validateModel(organization)).isNotEmpty();
    }

    @Test
    void organization_withEmptyFields_shouldHaveViolations() {
        Organization organization = new Organization();
        organization.setId("");

        assertThat(validateModel(organization)).isNotEmpty();
    }
}
