package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.LocationRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationAsyncServiceTest {
    @InjectMocks
    private LocationAsyncService locationAsyncService;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private QPortalApi qPortalApi;

    @Test
    void givenLocationWhenSaveThenVerifyDataSaveAndReturn() {
        // GIVEN:
        LocationType type = LocationType.CITY;
        String code = "TestCode";
        String name = "TestName";
        String externalId = "TestExternalId";
        String organizationId = "TestOrgId";
        LocationEntity expectedLocation = new LocationEntity();

        when(locationRepository.save(any(LocationEntity.class))).thenReturn(expectedLocation);

        // WHEN
        LocationEntity result = locationAsyncService.save(type, code, name, externalId, organizationId);

        // THEN
        verify(locationRepository, times(1)).save(any(LocationEntity.class));
        assertThat(result).isNotNull();
        assertThat(expectedLocation).isEqualTo(result);
    }

    @Test
    void givenLocationEntityExistingWhenCreateLocationThenNoNeedToCreateOrFindLocation() {
        // GIVEN:
        List<LocationEntity> locations = new ArrayList<>();
        LocationEntity existingLocation = new LocationEntity();
        existingLocation.setExternalId("ExistingExternalId");
        locations.add(existingLocation);
        String externalId = "ExistingExternalId";
        LocationType type = LocationType.CITY;
        String name = "TestName";
        String code = "TestCode";
        String organizationId = "TestOrgId";

        // WHEN:
        LocationEntity result = locationAsyncService.createLocation(locations, externalId, type, name, code, organizationId);

        // THEN:
        assertThat(result).isNotNull();
        verify(locationRepository, times(0)).findByExternalIdAndOrganizationId(any(), any());
        verify(locationRepository, times(0)).save(any());
        verifyNoInteractions(qPortalApi);
    }

    @Test
    void givenLocationNotExistingWhenCreateLocationThenRepositoryInvokeToSave() {
        // GIVEN:
        List<LocationEntity> locations = new ArrayList<>();
        String externalId = "NewExternalId";
        LocationType type = LocationType.CITY;
        String name = "TestName";
        String code = "TestCode";
        String organizationId = "TestOrgId";
        LocationEntity expectedLocation = new LocationEntity();

        when(locationRepository.findByExternalIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(expectedLocation);

        // WHEN:
        LocationEntity result = locationAsyncService.createLocation(locations, externalId, type, name, code, organizationId);

        // THEN:
        verify(locationRepository, times(1)).save(any(LocationEntity.class));
        assertThat(result).isNotNull();
        assertEquals(expectedLocation, result);
        verify(qPortalApi, times(1)).getLocation(organizationId, externalId);
    }
}
