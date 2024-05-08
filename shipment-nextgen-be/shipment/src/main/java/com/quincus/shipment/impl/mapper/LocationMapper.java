package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Location;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public class LocationMapper {

    public static LocationEntity mapDomainToEntity(final Location locationDomain) {
        if (isNull(locationDomain)) {
            return null;
        }

        LocationEntity locationEntity = new LocationEntity();

        locationEntity.setExternalId(locationDomain.getId());
        locationEntity.setTimezone(locationDomain.getTimezone());

        if (locationDomain.getCountry() != null) {
            locationEntity.setType(LocationType.COUNTRY);
            locationEntity.setCode(locationDomain.getCountry());
        } else if (locationDomain.getState() != null) {
            locationEntity.setType(LocationType.STATE);
            locationEntity.setCode(locationDomain.getState());
        } else if (locationDomain.getCity() != null) {
            locationEntity.setType(LocationType.CITY);
            locationEntity.setCode(locationDomain.getCity());
        } else if (locationDomain.getFacilityName() != null) {
            locationEntity.setType(LocationType.FACILITY);
            locationEntity.setCode(locationDomain.getFacilityName());
        }
        return locationEntity;
    }

    public static Location mapEntityToDomain(final LocationEntity locationEntity) {
        if (isNull(locationEntity)) {
            return null;
        }

        Location locationDomain = new Location();

        locationDomain.setId(locationEntity.getExternalId());
        locationDomain.setTimezone(locationEntity.getTimezone());
        locationDomain.setType(locationEntity.getType());

        if (LocationType.COUNTRY == locationEntity.getType()) {
            locationDomain.setCountry(locationEntity.getCode());
        } else if (LocationType.STATE == locationEntity.getType()) {
            locationDomain.setState(locationEntity.getCode());
        } else if (LocationType.CITY == locationEntity.getType()) {
            locationDomain.setCity(locationEntity.getCode());
        } else if (LocationType.FACILITY == locationEntity.getType()) {
            locationDomain.setFacilityName(locationEntity.getCode());
        }

        String organizationId = locationEntity.getOrganizationId();
        if (StringUtils.isNotEmpty(organizationId)) {
            locationDomain.setOrganizationId(organizationId);
        }

        return locationDomain;
    }
}
