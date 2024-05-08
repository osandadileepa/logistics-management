package com.quincus.shipment.impl.entity.component;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.valueobject.LocationHierarchyTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHierarchyTreeTest {
    private Tuple createLocationHierarchyEntityTuple(int level, String country, String state, String city, String facility) {
        LocationHierarchyEntity locationHierarchyEntity = new LocationHierarchyEntity();
        LocationEntity countryLocation = new LocationEntity();
        countryLocation.setCode(country);
        countryLocation.setName(country);
        countryLocation.setId(country);
        countryLocation.setType(LocationType.COUNTRY);
        locationHierarchyEntity.setCountryCode(country);

        locationHierarchyEntity.setCountry(countryLocation);
        LocationEntity stateLocation = new LocationEntity();
        stateLocation.setCode(state);
        stateLocation.setName(state);
        stateLocation.setId(state);
        stateLocation.setType(LocationType.STATE);
        locationHierarchyEntity.setState(stateLocation);
        locationHierarchyEntity.setStateCode(state);

        LocationEntity cityLocation = new LocationEntity();
        cityLocation.setCode(city);
        cityLocation.setName(city);
        cityLocation.setId(city);
        cityLocation.setType(LocationType.CITY);
        locationHierarchyEntity.setCity(cityLocation);
        locationHierarchyEntity.setCityCode(city);
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Id"), String.class)).thenReturn(countryLocation.getId());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Name"), String.class)).thenReturn(countryLocation.getName());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Code"), String.class)).thenReturn(countryLocation.getCode());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Description"), String.class)).thenReturn(countryLocation.getDescription());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("ExternalId"), String.class)).thenReturn(countryLocation.getExternalId());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Type"), LocationType.class)).thenReturn(countryLocation.getType());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("OrganizationId"), String.class)).thenReturn(countryLocation.getOrganizationId());
        when(tuple.get(LocationHierarchyEntity_.COUNTRY.concat("Timezone"), String.class)).thenReturn(countryLocation.getTimezone());
        if (level >= LocationType.STATE.value()) {
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Id"), String.class)).thenReturn(stateLocation.getId());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Name"), String.class)).thenReturn(stateLocation.getName());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Code"), String.class)).thenReturn(stateLocation.getCode());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Description"), String.class)).thenReturn(stateLocation.getDescription());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("ExternalId"), String.class)).thenReturn(stateLocation.getExternalId());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Type"), LocationType.class)).thenReturn(stateLocation.getType());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("OrganizationId"), String.class)).thenReturn(stateLocation.getOrganizationId());
            when(tuple.get(LocationHierarchyEntity_.STATE.concat("Timezone"), String.class)).thenReturn(stateLocation.getTimezone());
        }
        if (level >= LocationType.CITY.value()) {
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Id"), String.class)).thenReturn(cityLocation.getId());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Name"), String.class)).thenReturn(cityLocation.getName());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Code"), String.class)).thenReturn(cityLocation.getCode());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Description"), String.class)).thenReturn(cityLocation.getDescription());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("ExternalId"), String.class)).thenReturn(cityLocation.getExternalId());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Type"), LocationType.class)).thenReturn(cityLocation.getType());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("OrganizationId"), String.class)).thenReturn(cityLocation.getOrganizationId());
            when(tuple.get(LocationHierarchyEntity_.CITY.concat("Timezone"), String.class)).thenReturn(cityLocation.getTimezone());
        }
        if (StringUtils.isNotBlank(facility) && level >= LocationType.FACILITY.value()) {
            LocationEntity facilityLocation = new LocationEntity();
            facilityLocation.setCode(facility);
            facilityLocation.setName(facility);
            facilityLocation.setType(LocationType.FACILITY);
            facilityLocation.setId(facility);
            locationHierarchyEntity.setFacility(facilityLocation);
            locationHierarchyEntity.setFacilityCode(facility);
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Id"), String.class)).thenReturn(facilityLocation.getId());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Name"), String.class)).thenReturn(facilityLocation.getName());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Code"), String.class)).thenReturn(facilityLocation.getCode());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Description"), String.class)).thenReturn(facilityLocation.getDescription());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("ExternalId"), String.class)).thenReturn(facilityLocation.getExternalId());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Type"), LocationType.class)).thenReturn(facilityLocation.getType());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("OrganizationId"), String.class)).thenReturn(facilityLocation.getOrganizationId());
            when(tuple.get(LocationHierarchyEntity_.FACILITY.concat("Timezone"), String.class)).thenReturn(facilityLocation.getTimezone());
        }
        return tuple;
    }

    private List<Tuple> createLocationHierarchyEntitiesWithoutFacility(int level) {
        List<Tuple> locationHierarchyEntities = new ArrayList<>();
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state1", "country1-state1-city1", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state1", "country1-state1-city2", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state1", "country1-state1-city3", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state2", "country1-state2-city4", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state2", "country1-state2-city5", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country1", "country1-state2", "country1-state2-city6", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country2", "country2-state3", "country2-state3-city7", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country2", "country2-state3", "country2-state3-city8", null));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(level, "country2", "country2-state3", "country2-state3-city9", null));
        return locationHierarchyEntities;
    }

    private List<Tuple> createLocationHierarchyEntitiesWithFacility() {
        List<Tuple> locationHierarchyEntities = new ArrayList<>();
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state1", "country1-state1-city1", "country1-state1-city1-facility1"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state1", "country1-state1-city1", "country1-state1-city1-facility2"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state1", "country1-state1-city2", "country1-state1-city2-facility3"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state1", "country1-state1-city3", "country1-state1-city3-facility4"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state2", "country1-state2-city4", "country1-state1-city1-facility5"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state2", "country1-state2-city5", "country1-state2-city5-facility6"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country1", "country1-state2", "country1-state2-city6", "country1-state2-city6-facility7"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country2", "country2-state3", "country2-state3-city7", "country2-state3-city7-facility8"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country2", "country2-state3", "country2-state3-city8", "country2-state3-city8-facility9"));
        locationHierarchyEntities.add(createLocationHierarchyEntityTuple(4, "country2", "country2-state3", "country2-state3-city9", "country2-state3-city9-facility10"));
        return locationHierarchyEntities;
    }

    @Test
    void convertLocationHierarchyListToTree_withNullOrEmptyLocationHierarchyEntity_shouldReturnEmptyList() {
        List<LocationHierarchyTree> locationHierarchyTreeNull = LocationHierarchyTree.parseTreeList(null, 3);
        assertThat(locationHierarchyTreeNull).isEmpty();
        List<LocationHierarchyTree> locationHierarchyTreeEmpty = LocationHierarchyTree.parseTreeList(Collections.emptyList(), 3);
        assertThat(locationHierarchyTreeEmpty).isEmpty();
    }

    @Test
    void convertLocationHierarchyListToTree_withValidLocationHierarchyEntityList_shouldReturnListOfTrees() {
        List<Tuple> locationHierarchyEntities = createLocationHierarchyEntitiesWithoutFacility(3);
        List<LocationHierarchyTree> locationHierarchyTree = LocationHierarchyTree.parseTreeList(locationHierarchyEntities, 3);
        assertThat(locationHierarchyTree).hasSize(2);
        /*
         * Expects the following tree graph
         * country1
         *    - country1-state1
         *         - country1-state1-city1
         *         - country1-state1-city2
         *         - country1-state1-city3
         * country2
         *    - country2-state2
         *         - country2-state2-city4
         *         - country2-state2-city5
         *         - country2-state2-city6
         */
        LocationHierarchyTree country1 = locationHierarchyTree.get(0);
        assertThat(country1.getChildren()).hasSize(2);
        assertThat(country1.getCode()).isEqualTo("country1");
        List<LocationHierarchyTree> country1States = country1.getChildren();
        assertThat(country1States).hasSize(2);

        LocationHierarchyTree country1State1 = country1States.get(0);
        assertThat(country1State1.getChildren()).hasSize(3);
        assertThat(country1State1.getCode()).isEqualTo("country1-state1");
        assertThat(country1State1.getChildren().get(0).getCode()).isEqualTo("country1-state1-city1");
        assertThat(country1State1.getChildren().get(1).getCode()).isEqualTo("country1-state1-city2");
        assertThat(country1State1.getChildren().get(2).getCode()).isEqualTo("country1-state1-city3");
    }

    @Test
    void convertLocationHierarchyListToTree_getCountriesOnly_shouldReturnListOfTrees() {
        List<Tuple> locationHierarchyEntities = createLocationHierarchyEntitiesWithoutFacility(1);
        List<LocationHierarchyTree> locationHierarchyTree = LocationHierarchyTree.parseTreeList(locationHierarchyEntities, 1);
        assertThat(locationHierarchyTree).hasSize(2);
        LocationHierarchyTree country1 = locationHierarchyTree.get(0);
        assertThat(country1.getChildren()).isEmpty();
        assertThat(country1.getCode()).isEqualTo("country1");
        LocationHierarchyTree country2 = locationHierarchyTree.get(1);
        assertThat(country2.getChildren()).isEmpty();
        assertThat(country2.getCode()).isEqualTo("country2");
    }

    @Test
    void convertLocationHierarchyListToTree_getUpToStateValues_shouldReturnListOfTrees() {
        List<Tuple> locationHierarchyEntities = createLocationHierarchyEntitiesWithoutFacility(2);
        List<LocationHierarchyTree> locationHierarchyTree = LocationHierarchyTree.parseTreeList(locationHierarchyEntities, 2);

        LocationHierarchyTree country1 = locationHierarchyTree.get(0);
        assertThat(country1.getChildren()).hasSize(2);
        assertThat(country1.getCode()).isEqualTo("country1");
        List<LocationHierarchyTree> country1States = country1.getChildren();
        assertThat(country1States).hasSize(2);

        LocationHierarchyTree country1State1 = country1States.get(0);
        assertThat(country1State1.getChildren()).isEmpty();
        assertThat(country1State1.getCode()).isEqualTo("country1-state1");
    }

    @Test
    void convertLocationHierarchyListToTree_withValidLocationHierarchyEntityOfFacilities_shouldReturnListOfTrees() {
        List<Tuple> locationHierarchyEntities = createLocationHierarchyEntitiesWithFacility();

        List<LocationHierarchyTree> locationHierarchyTree = LocationHierarchyTree.parseTreeList(locationHierarchyEntities, 4);
        assertThat(locationHierarchyTree).hasSize(2);
        /* Expects the following tree graph
         * country1
         *    - country1-state1
         *         - country1-state1-city1
         *              - country1-state1-city1-facility1
         *              - country1-state1-city1-facility2
         *         - country1-state1-city2
         *              - country1-state1-city2-facility3
         *         - country1-state1-city3
         *              - country1-state1-city2-facility4
         * country2
         *    - country2-state2
         *         - country2-state2-city4
         *              - country2-state1-city2-facility3
         *         - country2-state2-city5
         *         - country2-state2-city6
         */
        LocationHierarchyTree country1 = locationHierarchyTree.get(0);
        assertThat(country1.getChildren()).hasSize(2);
        assertThat(country1.getCode()).isEqualTo("country1");

        List<LocationHierarchyTree> country1States = country1.getChildren();
        assertThat(country1States).hasSize(2);

        LocationHierarchyTree country1State1 = country1States.get(0);
        assertThat(country1State1.getChildren()).hasSize(3);
        assertThat(country1State1.getCode()).isEqualTo("country1-state1");
        assertThat(country1State1.getChildren().get(0).getCode()).isEqualTo("country1-state1-city1");
        assertThat(country1State1.getChildren().get(0).getCode()).isEqualTo("country1-state1-city1");
        assertThat(country1State1.getChildren().get(1).getCode()).isEqualTo("country1-state1-city2");
        assertThat(country1State1.getChildren().get(2).getCode()).isEqualTo("country1-state1-city3");

        //check facilities
        List<LocationHierarchyTree> country1State1City1Facilities = country1State1.getChildren().get(0).getChildren();
        assertThat(country1State1City1Facilities).hasSize(2);
        assertThat(country1State1City1Facilities.get(0).getCode()).isEqualTo("country1-state1-city1-facility1");
        assertThat(country1State1City1Facilities.get(1).getCode()).isEqualTo("country1-state1-city1-facility2");
    }
}