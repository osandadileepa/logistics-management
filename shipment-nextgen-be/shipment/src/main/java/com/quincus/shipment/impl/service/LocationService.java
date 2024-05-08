package com.quincus.shipment.impl.service;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.LocationFilter;
import com.quincus.shipment.impl.repository.LocationRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.quincus.shipment.api.constant.LocationType.CITY;
import static com.quincus.shipment.api.constant.LocationType.COUNTRY;
import static com.quincus.shipment.api.constant.LocationType.FACILITY;
import static com.quincus.shipment.api.constant.LocationType.STATE;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationService {
    private static final String ERR_UNABLE_TO_ENRICH_LOCATION_TIMEZONE_FROM_Q_PORTAL = "Unable to enrich location timezone from Q Portal with external id: `{}` and organization id: `{}`.";
    private final LocationRepository locationRepository;
    private final QPortalService qPortalService;
    private final UserDetailsProvider userDetailsProvider;

    @Transactional
    public LocationEntity findOrCreateLocation(final LocationType type,
                                               final String code,
                                               final String name,
                                               final String externalId) {
        Optional<LocationEntity> existingLocation = locationRepository.findByExternalIdAndOrganizationId(externalId, userDetailsProvider.getCurrentOrganizationId());
        return existingLocation.orElseGet(() -> save(type, code, name, externalId));
    }

    public List<LocationEntity> findByOrganizationIdAndExternalIds(final List<String> externalIds) {
        return locationRepository.findByOrganizationIdAndExternalIdIn(userDetailsProvider.getCurrentOrganizationId(), externalIds);
    }

    private LocationEntity save(
            @NotNull(message = "Location type is required.") final LocationType type,
            @NotBlank(message = "Location code is required.") final String code,
            @NotBlank(message = "Location code is required.") final String name,
            @NotBlank(message = "Location code is required.") final String externalId) {
        return locationRepository.save(createLocation(type, code, name, externalId));
    }

    @Transactional
    public LocationEntity createLocation(final List<LocationEntity> locations,
                                         final String externalId,
                                         final LocationType type,
                                         final String name,
                                         final String code) {
        if (Collections.isEmpty(locations) && StringUtils.isEmpty(externalId)) {
            return null;
        }
        LocationEntity location = locations.stream()
                .filter(Objects::nonNull)
                .filter(loc -> loc.getExternalId().equalsIgnoreCase(externalId)).findAny().orElse(null);
        if (!isNull(location)) {
            return location;
        }
        Optional<LocationEntity> optRecentLocation = locationRepository.findByExternalIdAndOrganizationId(externalId, userDetailsProvider.getCurrentOrganizationId());
        return optRecentLocation.orElseGet(() -> save(type, code, name, externalId));
    }

    private LocationEntity createLocation(final LocationType type,
                                          final String code,
                                          final String name,
                                          final String externalId) {
        LocationEntity location = new LocationEntity();
        location.setType(type);
        location.setCode(code);
        location.setName(name);
        location.setDescription(code);
        location.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        location.setExternalId(externalId);
        enrichLocationByQPortal(location);
        return location;
    }

    private void enrichLocationByQPortal(final LocationEntity location) {
        if (location == null || StringUtils.isBlank(location.getExternalId())) return;
        try {
            QPortalLocation qPortalLocation = qPortalService.getLocation(location.getExternalId());
            if (qPortalLocation != null) {
                location.setTimezone(qPortalLocation.getTimezoneTimeInGmt());
            }
        } catch (Exception e) {
            log.error(ERR_UNABLE_TO_ENRICH_LOCATION_TIMEZONE_FROM_Q_PORTAL, location.getExternalId(), userDetailsProvider.getCurrentOrganizationId());
        }
    }

    private Pageable buildPageable(final Filter filter) {
        String sortBy = filter.getSortBy();
        if (!StringUtils.equalsAny(filter.getSortBy(), LocationEntity_.NAME, LocationEntity_.CODE)) {
            sortBy = LocationEntity_.NAME;
        }
        return PageRequest.of((filter.getPage() - 1), filter.getPerPage(), Sort.by(Sort.Direction.ASC, sortBy));
    }

    public Page<LocationEntity> findPageableLocationsByFilter(final LocationFilter filter) {
        Pageable pageable = buildPageable(filter);
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        if (StringUtils.isEmpty(filter.getKey())) {
            return locationRepository.findByOrganizationIdAndType(filter.getType(), organizationId, pageable);
        }
        return locationRepository.findByOrganizationIdAndTypeAndCodeAndNameContaining(filter.getType(), organizationId, filter.getKey(), pageable);
    }

    @Transactional(readOnly = true)
    public Optional<LocationEntity> findLocationByExternalId(final String externalId) {
        return locationRepository.findByExternalIdAndOrganizationId(externalId, userDetailsProvider.getCurrentOrganizationId());
    }

    @Transactional
    public Optional<LocationEntity> createOrUpdateLocation(final QPortalLocation qPortalLocation) {
        final LocationType locationType = getLocationType(qPortalLocation);
        if (!EnumSet.of(COUNTRY, STATE, CITY, FACILITY).contains(locationType)) {
            return Optional.empty();
        }

        final LocationEntity location = findLocationByExternalId(qPortalLocation.getId())
                .orElse(new LocationEntity());
        location.setType(locationType);
        location.setCode(qPortalLocation.getLocationCode());
        location.setName(qPortalLocation.getName());
        location.setDescription(qPortalLocation.getLocationCode());
        location.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        location.setExternalId(qPortalLocation.getId());
        location.setTimezone(qPortalLocation.getTimezoneTimeInGmt());

        if (isBlank(location.getCode())
                || isBlank(location.getName())
                || isBlank(location.getExternalId())) {
            return Optional.empty();
        }
        return Optional.of(locationRepository.save(location));
    }

    public LocationType getLocationType(QPortalLocation qPortalLocation) {
        log.debug("Get LocationType from '{}'", qPortalLocation.getLocationType());
        if (StringUtils.isEmpty(qPortalLocation.getLocationType())) {
            return null;
        }

        String locationTypeString = qPortalLocation.getLocationType().toUpperCase();
        for (LocationType locationType : LocationType.values()) {
            if (locationTypeString.contains(locationType.name())) {
                return locationType;
            }
        }
        return null;
    }
}
