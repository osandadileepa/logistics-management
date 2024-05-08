package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.exception.LocationHierarchyDuplicateException;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@NoArgsConstructor
public class LocationHierarchyDuplicateValidator {
    private static final String DUPLICATE_LOCATION_FOUND_ERR_MSG = "Duplicate Location found in one of the Location hierarchy.";

    public void validateLocationHierarchy(LocationHierarchyEntity locationHierarchy) {
        if (locationHierarchy == null) return;
        List<LocationEntity> locationEntityList = Arrays.asList(locationHierarchy.getCountry(),
                locationHierarchy.getState(), locationHierarchy.getCity(), locationHierarchy.getFacility());

        long distinctLocationCount = locationEntityList.stream()
                .map(e -> String.format("%s-%s-%s", String.valueOf(e.getType().value())
                        , e.getCode(), e.getOrganizationId()))
                .distinct().count();

        if (distinctLocationCount != locationEntityList.size()) {
            throw new LocationHierarchyDuplicateException(DUPLICATE_LOCATION_FOUND_ERR_MSG);
        }
    }

}
