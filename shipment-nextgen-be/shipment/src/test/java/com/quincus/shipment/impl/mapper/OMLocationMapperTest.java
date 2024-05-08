package com.quincus.shipment.impl.mapper;

import com.quincus.order.api.domain.Location;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OMLocationMapperTest {
    @Test
    void testMapLocationToFacility_WithValidInputs_ShouldReturnFacility() {
        // Arrange
        String facilityId = "FAC001";
        String locationName = "LOC";
        Location location = new Location();
        location.setId("LOC001");

        // Act
        Facility facility = OMLocationMapper.mapLocationToFacility(facilityId, location, locationName);

        // Assert
        assertThat(facility).isNotNull();
        assertThat(facility.getName()).isEqualTo(locationName);
        assertThat(facility.getCode()).isEqualTo(locationName);
        assertThat(facility.getExternalId()).isEqualTo(facilityId);
        assertThat(facility.getLocation()).isNotNull();

    }

    @Test
    void testMapLocationToFacility_WithNullInputs_ShouldReturnNull() {
        // Act
        Facility facility = OMLocationMapper.mapLocationToFacility(null, null, null);

        // Assert
        assertThat(facility).isNull();
    }

    @Test
    void testMapToAddress_WithValidLocation_ShouldReturnAddress() {
        // Arrange
        Location location = new Location();
        location.setId(UUID.randomUUID().toString());
        location.setCountry("Country");
        location.setState("State");
        location.setCity("City");
        location.setAddressLine1("Address Line 1");
        location.setPostalCode("12345");

        // Act
        Address address = OMLocationMapper.mapToAddress(location);

        // Assert
        assertThat(address).isNotNull();
        assertThat(address.getExternalId()).isEqualTo(location.getId());
        assertThat(address.getCountryName()).isEqualTo("Country");
        assertThat(address.getStateName()).isEqualTo("State");
        assertThat(address.getCityName()).isEqualTo("City");
        assertThat(address.getLine1()).isEqualTo("Address Line 1");
        assertThat(address.getPostalCode()).isEqualTo("12345");
    }

    @Test
    void testMapToAddress_WithNullLocation_ShouldReturnNull() {
        assertThat(OMLocationMapper.mapToAddress(null)).isNull();
    }
}
