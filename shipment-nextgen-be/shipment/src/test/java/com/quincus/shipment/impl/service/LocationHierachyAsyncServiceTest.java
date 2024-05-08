package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.repository.LocationHierarchyRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHierachyAsyncServiceTest {
    private static final String countryId = "TestCountryId";
    private static final String countryCode = "TestCountryCode";
    private static final String countryName = "TestCountry";
    private static final String stateId = "TestStateId";
    private static final String stateCode = "TestStateCode";
    private static final String stateName = "TestState";
    private static final String cityId = "TestCityId";
    private static final String cityCode = "TestCityCode";
    private static final String organizationId = "TestOrgId";
    private static final String externalId = "TestExternalId";
    private static final String facilityName = "TestFacility";
    private static final String facilityCode = "TF";
    private static final String facilityLocationCode = "TFLoc";
    @Mock
    private LocationAsyncService locationAsyncService;
    @Mock
    private LocationHierarchyRepository locationHierarchyRepository;
    @InjectMocks
    private LocationHierarchyAsyncService locationHierarchyAsyncService;
    @Captor
    ArgumentCaptor<LocationHierarchyEntity> locationHierarchyEntityArgumentCaptor;

    @Test
    void givenNullFacilityWhenSetupLocationHierarchyThenReturnNullAndNoFutherProcess() {
        LocationHierarchyEntity result = locationHierarchyAsyncService.setUpLocationHierarchies((Facility) null, "TestOrgId");

        assertThat(result).isNull();
        verifyNoInteractions(locationAsyncService);
        verifyNoInteractions(locationHierarchyRepository);
    }

    @Test
    void givenNullAddressWhenSetupLocationHierarchyThenReturnNullAndNoFutherProcess() {
        LocationHierarchyEntity result = locationHierarchyAsyncService.setUpLocationHierarchies((Address) null, "TestOrgId");

        assertThat(result).isNull();
        verifyNoInteractions(locationAsyncService);
        verifyNoInteractions(locationHierarchyRepository);
    }

    @Test
    void givenCorrectFacilityWhenSetUpLocationHierarchiesThenLocationHierarchyCreated() {
        //GIVEN:
        List<String> externalIds = List.of(externalId, countryId, stateId, cityId);

        Facility facility = new Facility();
        facility.setName(facilityName);
        facility.setCode(facilityCode);
        facility.setLocationCode(facilityLocationCode);
        facility.setExternalId(externalId);

        Address address = new Address();
        address.setCountryId(countryId);
        address.setStateId(stateId);
        address.setCityId(cityId);
        address.setCountryName(countryName);
        address.setStateName(stateName);
        address.setCityName(cityCode);
        address.setExternalId(externalId);

        facility.setLocation(address);

        List<LocationEntity> locations = new ArrayList<>();
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setFacilityName(facilityName);
        locationHierarchyEntity.setFacilityCode(facilityCode);
        locationHierarchyEntity.setFacilityLocationCode(facilityLocationCode);
        locationHierarchyEntity.setCountryCode(countryCode);
        locationHierarchyEntity.setStateCode(stateCode);
        locationHierarchyEntity.setCityCode(cityCode);
        locationHierarchyEntity.setOrganizationId(organizationId);
        locationHierarchyEntity.setActive(true);
        locationHierarchyEntity.setExternalId(externalId);

        when(locationAsyncService.findByOrganizationIdAndExternalIds(externalIds, organizationId))
                .thenReturn(locations);
        when(locationHierarchyRepository.save(any(LocationHierarchyEntity.class)))
                .thenReturn(locationHierarchyEntity);
        when(locationAsyncService.createLocation(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(LocationEntity.class));


        // WHEN:
        locationHierarchyAsyncService.setUpLocationHierarchies(facility, organizationId);

        // THEN
        verify(locationHierarchyRepository).save(locationHierarchyEntityArgumentCaptor.capture());
        LocationHierarchyEntity result = locationHierarchyEntityArgumentCaptor.getValue();
        assertThat(result).isNotNull();
        assertThat(result.getFacilityName()).contains("TestFacility");
        assertThat(result.getFacilityCode()).contains("TF");
        assertThat(result.getFacilityLocationCode()).contains("TFLoc");
        assertThat(result.getCountryCode()).contains("TestCountry");
        assertThat(result.getStateCode()).contains("TestState");
        assertThat(result.getCityCode()).contains("TestCity");
        assertThat(result.getOrganizationId()).contains("TestOrgId");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getExternalId()).contains("TestExternalId");

        verify(locationAsyncService, times(1)).findByOrganizationIdAndExternalIds(externalIds, organizationId);
        verify(locationHierarchyRepository, times(1)).save(any(LocationHierarchyEntity.class));
    }

    @Test
    void givenCorrectAddressWhenSetUpLocationHierarchiesThenLocationHierarchyCreated() {

        // GIVEN:
        List<String> externalIds = List.of(countryId, stateId, cityId);

        Address address = new Address();
        address.setCountryId(countryId);
        address.setStateId(stateId);
        address.setCityId(cityId);
        address.setCountryName(countryName);
        address.setStateName(stateName);
        address.setCityName(cityCode);
        address.setExternalId(externalId);

        List<LocationEntity> locations = new ArrayList<>();
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        locationHierarchyEntity.setCountryCode(countryCode);
        locationHierarchyEntity.setStateCode(stateCode);
        locationHierarchyEntity.setCityCode(cityCode);
        locationHierarchyEntity.setActive(true);
        locationHierarchyEntity.setExternalId(externalId);
        locationHierarchyEntity.setOrganizationId(organizationId);

        when(locationAsyncService.findByOrganizationIdAndExternalIds(externalIds, organizationId))
                .thenReturn(locations);
        when(locationHierarchyRepository.save(any(LocationHierarchyEntity.class)))
                .thenReturn(locationHierarchyEntity);
        when(locationAsyncService.createLocation(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(LocationEntity.class));
        
        // WHEN:
        locationHierarchyAsyncService.setUpLocationHierarchies(address, organizationId);

        // THEN:
        verify(locationHierarchyRepository).save(locationHierarchyEntityArgumentCaptor.capture());
        LocationHierarchyEntity result = locationHierarchyEntityArgumentCaptor.getValue();
        assertThat(result).isNotNull();
        assertThat(result.getCountryCode()).contains("TestCountry");
        assertThat(result.getStateCode()).contains("TestState");
        assertThat(result.getCityCode()).contains("TestCity");
        assertThat(result.getOrganizationId()).contains("TestOrgId");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getExternalId()).contains("TestExternalId");

        verify(locationAsyncService, times(1)).findByOrganizationIdAndExternalIds(externalIds, organizationId);
        verify(locationHierarchyRepository, times(1)).save(any(LocationHierarchyEntity.class));
    }

}
