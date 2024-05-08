package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.exception.LocationHierarchyDuplicateException;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHierarchyDuplicateValidatorTest {

    @InjectMocks
    private LocationHierarchyDuplicateValidator validator;

    @Test
    @DisplayName("GIVEN no duplicate location in Hierarchy WHEN validateLocationHierarchy THEN do nothing")
    void validateLocationHierarchyDuplicate_validLocationHierarchy_shouldDoNothing() {
        //given
        String orgId = UUID.randomUUID().toString();
        LocationEntity country = createMockLocationEntity("PHILIPPINES", LocationType.COUNTRY, orgId);
        LocationEntity state = createMockLocationEntity("NCR", LocationType.STATE, orgId);
        LocationEntity city = createMockLocationEntity("MAKATI", LocationType.CITY, orgId);
        LocationEntity facility = createMockLocationEntity("MAKATI-HUB", LocationType.FACILITY, orgId);

        LocationHierarchyEntity locationHierarchy = createMockLocationHierarchyEntity();
        when(locationHierarchy.getCountry()).thenReturn(country);
        when(locationHierarchy.getFacility()).thenReturn(facility);
        when(locationHierarchy.getState()).thenReturn(state);
        when(locationHierarchy.getCity()).thenReturn(city);

        //when then assert no exception
        assertThatNoException().isThrownBy(() -> validator.validateLocationHierarchy(locationHierarchy));

    }

    @Test
    @DisplayName("GIVEN duplicateLocation in Hierarchy WHEN validateLocationHierarchy THEN LocationHierarchyDuplicateException")
    void validateLocationHierarchyDuplicate_locationHierarchyDuplicateLocation_shouldThrowLocationHierarchyDuplicateException() {
        //given
        String orgId = UUID.randomUUID().toString();
        LocationEntity country = createMockLocationEntity("PHILIPPINES", LocationType.COUNTRY, orgId);
        LocationEntity state = createMockLocationEntity("NCR", LocationType.STATE, orgId);
        LocationEntity city = createMockLocationEntity("MAKATI", LocationType.CITY, orgId);
        LocationEntity facility = createMockLocationEntity("MAKATI", LocationType.CITY, orgId);
        // Duplicate makati code with city location type

        LocationHierarchyEntity locationHierarchy = createMockLocationHierarchyEntity();
        when(locationHierarchy.getCountry()).thenReturn(country);
        when(locationHierarchy.getFacility()).thenReturn(facility);
        when(locationHierarchy.getState()).thenReturn(state);
        when(locationHierarchy.getCity()).thenReturn(city);

        //when then assert no exception
        assertThatThrownBy(() -> validator.validateLocationHierarchy(locationHierarchy))
                .isInstanceOf(LocationHierarchyDuplicateException.class)
                .hasMessage("Duplicate Location found in one of the Location hierarchy.");
    }

    private LocationHierarchyEntity createMockLocationHierarchyEntity() {
        return Mockito.mock(LocationHierarchyEntity.class);
    }

    private LocationEntity createMockLocationEntity(String code, LocationType type, String orgId) {
        LocationEntity entity = Mockito.mock(LocationEntity.class);
        when(entity.getCode()).thenReturn(code);
        when(entity.getType()).thenReturn(type);
        when(entity.getOrganizationId()).thenReturn(orgId);
        return entity;
    }

}
