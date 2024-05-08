package com.quincus.shipment.impl.service;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.LocationRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    private final String externalId = UUID.randomUUID().toString();
    private final LocationType locationType = LocationType.COUNTRY;
    private final String locationCode = "philippines1";
    private final String locationName = "Philippines";
    @InjectMocks
    private LocationService locationService;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    @DisplayName("Given no existing location WHEN findOrCreateLocation THEN create new location")
    void shouldCreateLocationWhenNoLocationFound() {
        String organizationId = UUID.randomUUID().toString();
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setTimezoneTimeInGmt("Asia/Manila UTC+08:00");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organization.getId())).thenReturn(Optional.empty());
        when(qPortalService.getLocation(externalId)).thenReturn(qPortalLocation);

        locationService.findOrCreateLocation(locationType, locationCode, locationName, externalId);

        verify(locationRepository, times(1)).save(any(LocationEntity.class));
        verify(qPortalService, times(1)).getLocation(anyString());
    }

    @Test
    @DisplayName("Given existing location WHEN findOrCreateLocation THEN retrieve location")
    void shouldFindLocationIfExist() {
        String organizationId = UUID.randomUUID().toString();
        LocationEntity locationEntity = new LocationEntity();
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);

        locationEntity.setId(externalId);
        locationEntity.setName(locationName);
        locationEntity.setCode(locationCode);
        locationEntity.setType(locationType);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organization.getId())).thenReturn(Optional.of(locationEntity));

        locationService.findOrCreateLocation(locationType, locationCode, locationName, externalId);

        assertThat(locationEntity.getId()).isEqualTo(externalId);
        assertThat(locationEntity.getCode()).isEqualTo(locationCode);
        assertThat(locationEntity.getName()).isEqualTo(locationName);
        assertThat(locationEntity.getType()).isEqualTo(locationType);
    }

    @Test
    void testGetLocationType_WithValidLocationType_ReturnsCorrectLocationType() {
        final String locationType = "Country";
        final QPortalLocation mockQPortalLocation = mock(QPortalLocation.class);
        when(mockQPortalLocation.getLocationType()).thenReturn(locationType);

        final LocationType result = locationService.getLocationType(mockQPortalLocation);

        assertThat(result).isEqualTo(LocationType.COUNTRY);
    }

    @Test
    void testGetLocationType_WithInvalidLocationType_ReturnsNull() {
        final String locationType = "Earth";
        final QPortalLocation mockQPortalLocation = mock(QPortalLocation.class);
        when(mockQPortalLocation.getLocationType()).thenReturn(locationType);

        final LocationType result = locationService.getLocationType(mockQPortalLocation);

        assertThat(result).isNull();
    }

    @Test
    void testGetLocationType_WithEmptyLocationType_ReturnsNull() {
        final String locationType = "";
        final QPortalLocation mockQPortalLocation = mock(QPortalLocation.class);
        when(mockQPortalLocation.getLocationType()).thenReturn(locationType);

        final LocationType result = locationService.getLocationType(mockQPortalLocation);

        assertThat(result).isNull();
    }

    @Test
    void testFindLocationByExternalId_WithExistingLocation_ReturnsLocationEntity() {
        final String externalId = "test-external-id";
        final String organizationId = "test-organization-id";
        final LocationEntity expectedLocationEntity = new LocationEntity();
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organizationId)).thenReturn(Optional.of(expectedLocationEntity));

        final Optional<LocationEntity> result = locationService.findLocationByExternalId(externalId);

        assertThat(result).isPresent().contains(expectedLocationEntity);
        verify(userDetailsProvider, times(1)).getCurrentOrganizationId();
        verify(locationRepository, times(1)).findByExternalIdAndOrganizationId(externalId, organizationId);
    }

    @Test
    void testFindLocationByExternalId_WithNonExistingLocation_ReturnsEmptyOptional() {
        final String externalId = "non-existing-external-id";
        final String organizationId = "test-organization-id";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organizationId)).thenReturn(Optional.empty());

        Optional<LocationEntity> result = locationService.findLocationByExternalId(externalId);

        assertThat(result).isEmpty();
        verify(userDetailsProvider, times(1)).getCurrentOrganizationId();
        verify(locationRepository, times(1)).findByExternalIdAndOrganizationId(externalId, organizationId);
    }

    @Test
    void testCreateOrUpdateLocation_WithInvalidLocationType_ReturnsEmptyOptional() {
        final QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setLocationType("invalid_type");

        Optional<LocationEntity> result = locationService.createOrUpdateLocation(qPortalLocation);

        assertThat(result).isEmpty();
        verify(userDetailsProvider, never()).getCurrentOrganizationId();
        verify(locationRepository, never()).findByExternalIdAndOrganizationId(anyString(), anyString());
        verify(locationRepository, never()).save(any(LocationEntity.class));
    }

    @Test
    void testCreateOrUpdateLocation_WithValidLocationTypeAndExistingLocation_ReturnsUpdatedLocation() {
        final QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setLocationCode("CODE");
        qPortalLocation.setLocationType("City");
        final String externalId = "test-external-id";
        qPortalLocation.setId(externalId);
        final String newName = "test-new-name";
        qPortalLocation.setName(newName);
        final String organizationId = "test-organization-id";
        final LocationEntity existingLocation = new LocationEntity();
        existingLocation.setExternalId(externalId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organizationId)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(existingLocation);

        final Optional<LocationEntity> result = locationService.createOrUpdateLocation(qPortalLocation);

        assertThat(result).isPresent().contains(existingLocation);
        verify(userDetailsProvider, times(2)).getCurrentOrganizationId();
        verify(locationRepository, times(1)).findByExternalIdAndOrganizationId(externalId, organizationId);

        final ArgumentCaptor<LocationEntity> captor = ArgumentCaptor.forClass(LocationEntity.class);
        verify(locationRepository, times(1)).save(captor.capture());
        final LocationEntity actual = captor.getValue();
        assertThat(actual.getName()).isEqualTo(newName);
    }

    @Test
    void testCreateOrUpdateLocation_WithValidLocationTypeAndNewLocation_ReturnsNewLocation() {
        final String externalId = "test-external-id";
        final String organizationId = "test-organization-id";
        final QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setLocationType("Facility");
        qPortalLocation.setLocationCode("CODE");
        qPortalLocation.setName("Loc Name");
        qPortalLocation.setId(externalId);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(locationRepository.findByExternalIdAndOrganizationId(externalId, organizationId)).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(new LocationEntity());

        final Optional<LocationEntity> result = locationService.createOrUpdateLocation(qPortalLocation);

        assertThat(result).isPresent();
        verify(userDetailsProvider, times(2)).getCurrentOrganizationId();
        verify(locationRepository, times(1)).findByExternalIdAndOrganizationId(externalId, organizationId);

        final ArgumentCaptor<LocationEntity> captor = ArgumentCaptor.forClass(LocationEntity.class);
        verify(locationRepository, times(1)).save(captor.capture());
        final LocationEntity actual = captor.getValue();
        assertThat(actual.getExternalId()).isEqualTo(externalId);
        assertThat(actual.getOrganizationId()).isEqualTo(organizationId);
    }
}
