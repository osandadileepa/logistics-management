package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityAsyncServiceTest {
    @InjectMocks
    private FacilityAsyncService facilityAsyncService;
    @Mock
    private QPortalApi qPortalApi;

    @Test
    void testGenerateFacilityFromQPortal_ValidFacility() {
        // GIVEN:
        String facilityName = "facilityName";
        String organizationId = "TestOrgId";
        QPortalLocation facilityQPortalLocation = new QPortalLocation();
        facilityQPortalLocation.setLocationType(LocationType.FACILITY.name());
        facilityQPortalLocation.setName("TestFacility");
        facilityQPortalLocation.setCode("TestCode");
        facilityQPortalLocation.setLocationCode("TestLocationCode");
        facilityQPortalLocation.setId("TestLocationId");
        facilityQPortalLocation.setCountryId("TestCountryId");
        facilityQPortalLocation.setCityId("TestCityId");
        facilityQPortalLocation.setStateProvinceId("TestStateId");
        facilityQPortalLocation.setAddress1("TestAddress1");
        facilityQPortalLocation.setAddress2("TestAddress2");
        facilityQPortalLocation.setAddress3("TestAddress3");
        facilityQPortalLocation.setAncestors("TestCountryName, TestStateName, TestCityName");
        when(qPortalApi.getLocationsByName(organizationId, facilityName)).thenReturn(List.of(facilityQPortalLocation));

        // WHEN
        Facility result = facilityAsyncService.generateFacilityFromQPortal(facilityName, organizationId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).contains("TestFacility");
        assertThat(result.getCode()).contains("TestCode");
        assertThat(result.getLocationCode()).contains("TestLocationCode");
        assertThat(result.getExternalId()).contains("TestLocationId");
        Address facilityAddress = result.getLocation();
        assertThat(facilityAddress).isNotNull();
        assertThat(facilityAddress.getCountryId()).contains("TestCountryId");
        assertThat(facilityAddress.getCityId()).contains("TestCityId");
        assertThat(facilityAddress.getStateId()).contains("TestStateId");
        assertThat(facilityAddress.getLine1()).contains("TestAddress1");
        assertThat(facilityAddress.getLine2()).contains("TestAddress2");
        assertThat(facilityAddress.getLine3()).contains("TestAddress3");
        verify(qPortalApi, times(1)).getLocationsByName(organizationId, facilityName);
    }

    @Test
    void testGenerateFacilityFromQPortal_NullExternalId() {
        // GIVEN:
        String externalId = null;
        String organizationId = "TestOrgId";

        //WHEN THEN:
        assertThatThrownBy(() -> facilityAsyncService.generateFacilityFromQPortal(null, organizationId))
                .isInstanceOf(QuincusValidationException.class).hasMessage("Facility Name: `null` is not a valid Facility.");

        verify(qPortalApi, never()).getLocation(any(), any());
    }

    @Test
    void testGenerateFacilityFromQPortal_InvalidQPortalLocation() {
        // GIVEN:
        String facilityName = "facilityName";
        String organizationId = "TestOrgId";
        when(qPortalApi.getLocationsByName(organizationId, facilityName)).thenReturn(null);

        // WHEN THEN:
        assertThatThrownBy(() -> facilityAsyncService.generateFacilityFromQPortal(facilityName, organizationId))
                .isInstanceOf(QuincusException.class)
                .hasMessage("Facility Name: `facilityName` is not a valid Facility.");

        verify(qPortalApi, times(1)).getLocationsByName(organizationId, facilityName);
    }

    @Test
    void testGenerateFacilityFromQPortal_NonFacilityLocation() {
        // GIVEN:
        String facilityName = "facilityName";
        String organizationId = "TestOrgId";
        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setLocationType("Other");
        when(qPortalApi.getLocationsByName(organizationId, facilityName)).thenReturn(List.of(qPortalLocation));

        // WHEN THEN:
        assertThatThrownBy(() -> facilityAsyncService.generateFacilityFromQPortal(facilityName, organizationId))
                .isInstanceOf(QuincusException.class)
                .hasMessage("Facility Name: `facilityName` is not a valid Facility.");

        verify(qPortalApi, times(1)).getLocationsByName(organizationId, facilityName);
    }

}