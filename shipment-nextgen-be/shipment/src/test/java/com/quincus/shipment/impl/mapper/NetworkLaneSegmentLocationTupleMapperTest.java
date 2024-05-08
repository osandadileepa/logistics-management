package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;

import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.CITY;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.COUNTRY;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.EXTERNAL_ID;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.FACILITY;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.ID;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.LOCATION_HIERARCHY_CITY_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.LOCATION_HIERARCHY_COUNTRY_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.LOCATION_HIERARCHY_FACILITY_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.LOCATION_HIERARCHY_FACILITY_LOCATION_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.LOCATION_HIERARCHY_STATE_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.NAME;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneSegmentLocationTupleMapperTest {
    private final NetworkLaneSegmentLocationTupleMapper networkLaneSegmentLocationTupleMapper = new NetworkLaneSegmentLocationTupleMapper();

    @Test
    void giveTuple_whenValuePresent_thenMapToPartner() {
        Tuple tuple = mock(Tuple.class);
        String aliasPrefix = "start";
        String lhAlias = aliasPrefix.concat("LH");
        when(tuple.get(lhAlias.concat(ID), String.class)).thenReturn("lh-id");
        when(tuple.get(lhAlias.concat(LOCATION_HIERARCHY_COUNTRY_CODE), String.class)).thenReturn("PH");
        when(tuple.get(aliasPrefix.concat(COUNTRY).concat(ID), String.class)).thenReturn("country-id");
        when(tuple.get(aliasPrefix.concat(COUNTRY).concat(EXTERNAL_ID), String.class)).thenReturn("country-ext-id");
        when(tuple.get(aliasPrefix.concat(COUNTRY).concat(NAME), String.class)).thenReturn("country-name");

        when(tuple.get(lhAlias.concat(LOCATION_HIERARCHY_STATE_CODE), String.class)).thenReturn("MM");
        when(tuple.get(aliasPrefix.concat(STATE).concat(ID), String.class)).thenReturn("state-id");
        when(tuple.get(aliasPrefix.concat(STATE).concat(EXTERNAL_ID), String.class)).thenReturn("state-ext-id");
        when(tuple.get(aliasPrefix.concat(STATE).concat(NAME), String.class)).thenReturn("state-name");

        when(tuple.get(lhAlias.concat(LOCATION_HIERARCHY_CITY_CODE), String.class)).thenReturn("Mandaluyong");
        when(tuple.get(aliasPrefix.concat(CITY).concat(ID), String.class)).thenReturn("city-id");
        when(tuple.get(aliasPrefix.concat(CITY).concat(EXTERNAL_ID), String.class)).thenReturn("city-ext-id");
        when(tuple.get(aliasPrefix.concat(CITY).concat(NAME), String.class)).thenReturn("city-name");

        when(tuple.get(lhAlias.concat(LOCATION_HIERARCHY_FACILITY_CODE), String.class)).thenReturn("JREpress");
        when(tuple.get(aliasPrefix.concat(FACILITY).concat(ID), String.class)).thenReturn("facility-id");
        when(tuple.get(aliasPrefix.concat(FACILITY).concat(EXTERNAL_ID), String.class)).thenReturn("facility-ext-id");
        when(tuple.get(aliasPrefix.concat(FACILITY).concat(NAME), String.class)).thenReturn("facility-name");

        when(tuple.get(lhAlias.concat(LOCATION_HIERARCHY_FACILITY_LOCATION_CODE), String.class)).thenReturn("JExpress");

        LocationHierarchyEntity entity = networkLaneSegmentLocationTupleMapper.toEntity(tuple, aliasPrefix);
        assertLocationHierarchy(entity);
    }

    private void assertLocationHierarchy(LocationHierarchyEntity locationHierarchy) {
        assertThat(locationHierarchy).isNotNull();
        assertThat(locationHierarchy.getId()).isNotBlank();
        assertThat(locationHierarchy.getCountryCode()).isEqualTo("PH");
        assertThat(locationHierarchy.getStateCode()).isEqualTo("MM");
        assertThat(locationHierarchy.getCityCode()).isEqualTo("Mandaluyong");
        assertThat(locationHierarchy.getFacilityCode()).isEqualTo("JREpress");
        assertThat(locationHierarchy.getFacilityLocationCode()).isEqualTo("JExpress");

        assertThat(locationHierarchy.getCountry()).isNotNull();
        assertThat(locationHierarchy.getCountry().getId()).isEqualTo("country-id");
        assertThat(locationHierarchy.getCountry().getExternalId()).isEqualTo("country-ext-id");
        assertThat(locationHierarchy.getCountry().getName()).isEqualTo("country-name");

        assertThat(locationHierarchy.getState()).isNotNull();
        assertThat(locationHierarchy.getState().getId()).isEqualTo("state-id");
        assertThat(locationHierarchy.getState().getExternalId()).isEqualTo("state-ext-id");
        assertThat(locationHierarchy.getState().getName()).isEqualTo("state-name");

        assertThat(locationHierarchy.getCity()).isNotNull();
        assertThat(locationHierarchy.getCity().getId()).isEqualTo("city-id");
        assertThat(locationHierarchy.getCity().getExternalId()).isEqualTo("city-ext-id");
        assertThat(locationHierarchy.getCity().getName()).isEqualTo("city-name");

        assertThat(locationHierarchy.getFacility()).isNotNull();
        assertThat(locationHierarchy.getFacility().getId()).isEqualTo("facility-id");
        assertThat(locationHierarchy.getFacility().getExternalId()).isEqualTo("facility-ext-id");
        assertThat(locationHierarchy.getFacility().getName()).isEqualTo("facility-name");
    }
}
