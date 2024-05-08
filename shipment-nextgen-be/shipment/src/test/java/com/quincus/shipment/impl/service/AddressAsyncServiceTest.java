package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressAsyncServiceTest {
    @InjectMocks
    private AddressAsyncService addressAsyncService;
    @Mock
    private QPortalApi qPortalApi;

    @Test
    void givenCityExistInQPortalWhenGenerateCityAddressFromQPortalThenProperlyMapToAddress() {
        // Mock the QPortalLocation response
        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setId("1");
        qPortalLocation.setName("City Name");
        qPortalLocation.setLocationType(LocationType.CITY.name());
        qPortalLocation.setCountryId("2");
        qPortalLocation.setStateProvinceId("3");
        qPortalLocation.setAddress1("Address Line 1");
        qPortalLocation.setAddress2("Address Line 2");
        qPortalLocation.setAddress3("Address Line 3");
        qPortalLocation.setAncestors("Country, State");

        when(qPortalApi.getLocationsByName(anyString(), anyString())).thenReturn(List.of(qPortalLocation));

        // Generate the city address
        Address cityAddress = addressAsyncService.generateCityAddressFromQPortal("City Name", "State", "Country", "Organization ID");

        // Verify the city address properties
        assertThat(cityAddress).isNotNull();
        assertThat(cityAddress.getCityId()).contains("1");
        assertThat(cityAddress.getCityName()).contains("City Name");
        assertThat(cityAddress.getCountryId()).contains("2");
        assertThat(cityAddress.getCountryName()).contains("Country");
        assertThat(cityAddress.getStateId()).contains("3");
        assertThat(cityAddress.getStateName()).contains("State");
        assertThat(cityAddress.getLine1()).contains("Address Line 1");
        assertThat(cityAddress.getLine2()).contains("Address Line 2");
        assertThat(cityAddress.getLine3()).contains("Address Line 3");
        assertThat(cityAddress.getExternalId()).contains("1");
    }

    @Test
    void givenMultipleCityExistInQPortalWhenGenerateCityAddressFromQPortalThenProperlyMapToAddressSameState() {
        // Mock the QPortalLocation response
        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setId("1");
        qPortalLocation.setName("City Name");
        qPortalLocation.setLocationType(LocationType.CITY.name());
        qPortalLocation.setCountryId("2");
        qPortalLocation.setStateProvinceId("3");
        qPortalLocation.setAddress1("Address Line 1");
        qPortalLocation.setAddress2("Address Line 2");
        qPortalLocation.setAddress3("Address Line 3");
        qPortalLocation.setAncestors("Country, State");

        QPortalLocation qPortalLocation2 = new QPortalLocation();
        qPortalLocation2.setId("2");
        qPortalLocation2.setName("City Name");
        qPortalLocation2.setLocationType(LocationType.CITY.name());
        qPortalLocation2.setCountryId("2");
        qPortalLocation2.setStateProvinceId("4");
        qPortalLocation2.setAddress1("Address Line 11");
        qPortalLocation2.setAddress2("Address Line 22");
        qPortalLocation2.setAddress3("Address Line 33");
        qPortalLocation2.setAncestors("Country, State2");

        when(qPortalApi.getLocationsByName(anyString(), anyString())).thenReturn(List.of(qPortalLocation, qPortalLocation2));

        // Generate the city address
        Address cityAddress = addressAsyncService.generateCityAddressFromQPortal("City Name", "State2", "Country", "Organization ID");

        // Verify the city address properties
        assertThat(cityAddress).isNotNull();
        assertThat(cityAddress.getCityId()).contains("2");
        assertThat(cityAddress.getCityName()).contains("City Name");
        assertThat(cityAddress.getCountryId()).contains("2");
        assertThat(cityAddress.getCountryName()).contains("Country");
        assertThat(cityAddress.getStateId()).contains("4");
        assertThat(cityAddress.getStateName()).contains("State");
        assertThat(cityAddress.getLine1()).contains("Address Line 11");
        assertThat(cityAddress.getLine2()).contains("Address Line 22");
        assertThat(cityAddress.getLine3()).contains("Address Line 33");
        assertThat(cityAddress.getExternalId()).contains("2");
    }

    @Test
    void givenCityNameAndQportalReturnedNonCityWhenGenerateCityAddressFromQPortalThenShouldThrowException() {
        // Mock the QPortalLocation response for an invalid city
        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setLocationType(LocationType.COUNTRY.name());

        when(qPortalApi.getLocationsByName(anyString(), anyString())).thenReturn(List.of(qPortalLocation));

        // Verify that generating the city address throws a QuincusException
        QuincusException exception = assertThrows(QuincusException.class, () ->
                addressAsyncService.generateCityAddressFromQPortal("Invalid City", "Invalid State", "Invalid Country", "Organization ID"));

        // Verify the exception message
        assertThat(exception.getMessage()).isEqualTo("City:`Invalid City` is not valid.");
    }

    @Test
    void givenCityNotExistInQPortalWhenGenerateCityAddressFromQPortalThenThrowQuincusException() {
        // GIVEN:
        String cityName = "NonexistentCity";
        String stateName = "stateName";
        String countryName = "countryName";
        String organizationId = "TestOrgId";

        when(qPortalApi.getLocationsByName(organizationId, cityName)).thenReturn(null);

        // WHEN:
        assertThatThrownBy(() -> addressAsyncService.generateCityAddressFromQPortal(cityName, stateName, countryName, organizationId))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessage("City:`NonexistentCity` is not valid.");

        // THEN:
        verify(qPortalApi, times(1)).getLocationsByName(organizationId, cityName);
    }
}
