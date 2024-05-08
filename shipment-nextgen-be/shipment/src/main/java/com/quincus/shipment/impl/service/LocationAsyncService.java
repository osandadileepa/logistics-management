package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.LocationRepository;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationAsyncService {
    private static final String ERR_UNABLE_TO_ENRICH_LOCATION_TIMEZONE_FROM_Q_PORTAL = "Unable to enrich location timezone from Q Portal with external id: `{}` and organization id: `{}`.";
    private final LocationRepository locationRepository;
    private final QPortalApi qPortalApi;

    public LocationEntity save(final LocationType type,
                               final String code,
                               final String name,
                               final String externalId,
                               final String organizationId) {
        return locationRepository.save(createLocation(type, code, name, externalId, organizationId));
    }

    @Transactional
    public LocationEntity createLocation(final List<LocationEntity> locations,
                                         final String externalId,
                                         final LocationType type,
                                         final String name,
                                         final String code,
                                         final String organizationId) {
        if (Collections.isEmpty(locations) && StringUtils.isEmpty(externalId)) {
            return null;
        }
        LocationEntity location = locations.stream()
                .filter(Objects::nonNull)
                .filter(loc -> loc.getExternalId().equalsIgnoreCase(externalId)).findAny().orElse(null);
        if (!isNull(location)) {
            return location;
        }
        Optional<LocationEntity> optRecentLocation = locationRepository.findByExternalIdAndOrganizationId(externalId, organizationId);
        return optRecentLocation.orElseGet(() -> save(type, code, name, externalId, organizationId));
    }

    private LocationEntity createLocation(final LocationType type,
                                          final String code,
                                          final String name,
                                          final String externalId,
                                          final String organizationId) {
        LocationEntity location = new LocationEntity();
        location.setType(type);
        location.setCode(code);
        location.setName(name);
        location.setDescription(code);
        location.setOrganizationId(organizationId);
        location.setExternalId(externalId);
        enrichLocationByQPortal(location, organizationId);
        return location;
    }

    public List<LocationEntity> findByOrganizationIdAndExternalIds(final List<String> externalIds, String organizationId) {
        return locationRepository.findByOrganizationIdAndExternalIdIn(organizationId, externalIds);
    }

    private void enrichLocationByQPortal(final LocationEntity location, String organizationId) {
        if (location == null || StringUtils.isBlank(location.getExternalId())) return;
        try {
            QPortalLocation qPortalLocation = qPortalApi.getLocation(organizationId, location.getExternalId());
            if (qPortalLocation != null) {
                location.setTimezone(qPortalLocation.getTimezoneTimeInGmt());
            }
        } catch (Exception e) {
            log.error(ERR_UNABLE_TO_ENRICH_LOCATION_TIMEZONE_FROM_Q_PORTAL, location.getExternalId(), location.getOrganizationId());
        }
    }
}
