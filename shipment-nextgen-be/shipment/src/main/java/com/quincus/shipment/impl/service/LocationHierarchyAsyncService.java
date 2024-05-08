package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.repository.LocationHierarchyRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.LocationType.CITY;
import static com.quincus.shipment.api.constant.LocationType.COUNTRY;
import static com.quincus.shipment.api.constant.LocationType.FACILITY;
import static com.quincus.shipment.api.constant.LocationType.STATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationHierarchyAsyncService {

    private final LocationAsyncService locationAsyncService;
    private final LocationHierarchyRepository locationHierarchyRepository;

    @Transactional
    public LocationHierarchyEntity setUpLocationHierarchies(final Facility facility,
                                                            final String organizationId) {
        if (isNull(facility) || StringUtils.isBlank(organizationId)) {
            return null;
        }
        return generateLocationHierarchyFromFacility(facility, organizationId);
    }

    @Transactional
    public LocationHierarchyEntity setUpLocationHierarchies(final Address address,
                                                            final String organizationId) {
        if (isNull(address) || StringUtils.isBlank(organizationId)) {
            return null;
        }
        return generateLocationHierarchyFromAddress(address, organizationId);
    }

    private LocationHierarchyEntity generateLocationHierarchyFromAddress(final Address address, final String organizationId) {
        List<String> externalIds = Stream.of(address.getCountryId(), address.getStateId(), address.getCityId()).filter(Objects::nonNull).toList();
        final List<LocationEntity> locations = new ArrayList<>(locationAsyncService.findByOrganizationIdAndExternalIds(externalIds, organizationId));
        LocationHierarchyEntity locationHierarchy = constructLocationHierarchy(locations, address, organizationId);
        return findOrCreateLocationHierarchy(locationHierarchy, organizationId);
    }

    private LocationEntity createLocationCountry(final List<LocationEntity> locations,
                                                 final Address address, final String organizationId) {
        return locationAsyncService.createLocation(locations, address.getCountryId(),
                COUNTRY, address.getCountryName(), address.getCountryName(), organizationId);
    }

    private LocationEntity createLocationState(final List<LocationEntity> locations,
                                               final Address address, String organizationId) {
        return locationAsyncService.createLocation(locations, address.getStateId(),
                STATE, address.getStateName(), address.getStateName(), organizationId);
    }

    private LocationEntity createLocationCity(final List<LocationEntity> locations,
                                              final Address address, final String organizationId) {
        return locationAsyncService.createLocation(locations, address.getCityId(),
                CITY, address.getCityName(), address.getCityName(), organizationId);
    }

    private LocationEntity createLocationFacility(final List<LocationEntity> locations,
                                                  final Facility facility, final String organizationId) {
        return locationAsyncService.createLocation(locations, facility.getExternalId(), FACILITY, facility.getName(), facility.getLocationCode(), organizationId);
    }

    private LocationHierarchyEntity constructLocationHierarchy(final List<LocationEntity> locations,
                                                               final Facility facility,
                                                               final String organizationId) {

        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setCountry(createLocationCountry(locations, facility.getLocation(), organizationId));
        locationHierarchy.setState(createLocationState(locations, facility.getLocation(), organizationId));
        locationHierarchy.setCity(createLocationCity(locations, facility.getLocation(), organizationId));
        locationHierarchy.setFacility(createLocationFacility(locations, facility, organizationId));
        locationHierarchy.setFacilityName(facility.getName());
        locationHierarchy.setFacilityCode(facility.getCode());
        locationHierarchy.setFacilityLocationCode(facility.getLocationCode());
        locationHierarchy.setCountryCode(facility.getLocation().getCountryName());
        locationHierarchy.setStateCode(facility.getLocation().getStateName());
        locationHierarchy.setCityCode(facility.getLocation().getCityName());
        locationHierarchy.setOrganizationId(organizationId);
        locationHierarchy.setActive(true);
        locationHierarchy.setExternalId(facility.getExternalId());
        return locationHierarchy;
    }

    private LocationHierarchyEntity constructLocationHierarchy(final List<LocationEntity> locations,
                                                               final Address address,
                                                               final String organizationId) {

        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setCountry(createLocationCountry(locations, address, organizationId));
        locationHierarchy.setState(createLocationState(locations, address, organizationId));
        locationHierarchy.setCity(createLocationCity(locations, address, organizationId));
        locationHierarchy.setCountryCode(address.getCountryName());
        locationHierarchy.setStateCode(address.getStateName());
        locationHierarchy.setCityCode(address.getCityName());
        locationHierarchy.setOrganizationId(organizationId);
        locationHierarchy.setActive(true);
        locationHierarchy.setExternalId(address.getExternalId());
        return locationHierarchy;
    }

    private LocationHierarchyEntity generateLocationHierarchyFromFacility(final Facility facility, final String organizationId) {
        List<String> externalIds = extractExternalIds(facility).stream().filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(externalIds)) {
            return null;
        }
        final List<LocationEntity> locations = new ArrayList<>(locationAsyncService.findByOrganizationIdAndExternalIds(externalIds, organizationId));
        LocationHierarchyEntity locationHierarchy = constructLocationHierarchy(locations, facility, organizationId);
        return findOrCreateLocationHierarchy(locationHierarchy, organizationId);
    }

    private LocationHierarchyEntity findOrCreateLocationHierarchy(final LocationHierarchyEntity locationHierarchy, String organizationId) {

        String facilityId = getLocationEntityId(locationHierarchy.getFacility());
        LocationHierarchyEntity locationHierarchyResult = findLocationHierarchyEntity(locationHierarchy.getCountry()
                , locationHierarchy.getCity(), locationHierarchy.getState(), facilityId, organizationId);
        if (locationHierarchyResult != null) return locationHierarchyResult;

        return locationHierarchyRepository.save(locationHierarchy);
    }

    private LocationHierarchyEntity findLocationHierarchyEntity(final LocationEntity country,
                                                                final LocationEntity city,
                                                                final LocationEntity state,
                                                                final String facilityId,
                                                                final String organizationId) {
        if (nonNull(facilityId)) {
            return locationHierarchyRepository.findByCountryAndStateAndCityAndFacilityAndOrganizationId(
                    getLocationEntityId(country),
                    getLocationEntityId(state),
                    getLocationEntityId(city),
                    facilityId,
                    organizationId).orElse(null);
        }
        return locationHierarchyRepository.findLHWithoutFacility(
                getLocationEntityId(country),
                getLocationEntityId(state),
                getLocationEntityId(city),
                organizationId).orElse(null);
    }

    private String getLocationEntityId(final LocationEntity locationEntity) {
        return nonNull(locationEntity) ? locationEntity.getId() : null;
    }

    private List<String> extractExternalIds(Facility facility) {
        if (facility.getLocation() == null) {
            return List.of(facility.getExternalId());
        }
        return List.of(facility.getExternalId(), facility.getLocation().getCountryId()
                , facility.getLocation().getStateId(), facility.getLocation().getCityId());
    }

}
