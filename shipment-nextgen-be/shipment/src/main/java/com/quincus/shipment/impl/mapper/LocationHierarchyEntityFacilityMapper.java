package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@AllArgsConstructor
public class LocationHierarchyEntityFacilityMapper {

    private final LocationHierarchyEntityAddressMapper lhToAddressMapper;

    public Facility mapLocationHierarchyToFacility(LocationHierarchyEntity lh) {
        if (isNull(lh)) return null;
        Facility facility = new Facility();

        if (nonNull(lh.getFacility())) {
            facility.setId(lh.getFacility().getId());
            facility.setExternalId(lh.getFacility().getExternalId());
            facility.setName(lh.getFacility().getName());
            facility.setCode(lh.getFacilityCode());
            facility.setLocationCode(lh.getFacilityLocationCode());
        } else {
            facility.setId(lh.getId());
            facility.setExternalId(lh.getExternalId());
        }
        facility.setLocation(lhToAddressMapper.mapLocationHierarchyToAddress(lh));
        return facility;
    }
}
