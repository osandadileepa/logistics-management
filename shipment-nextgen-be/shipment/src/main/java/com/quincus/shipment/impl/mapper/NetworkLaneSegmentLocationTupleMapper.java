package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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

@Component
public class NetworkLaneSegmentLocationTupleMapper {
    private static final String LOC_HIERARCHY_PREFIX_ALIAS = "LH";

    public LocationHierarchyEntity toEntity(Tuple tuple, String prefixAlias) {
        String locationHierarchyAlias = prefixAlias.concat(LOC_HIERARCHY_PREFIX_ALIAS);
        String id = tuple.get(locationHierarchyAlias.concat(ID), String.class);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        LocationHierarchyEntity locationHierarchy = new LocationHierarchyEntity();
        locationHierarchy.setId(id);
        if (tupleHasLocationId(tuple, prefixAlias, COUNTRY)) {
            locationHierarchy.setCountry(mapTupleToLocation(tuple, prefixAlias, COUNTRY));
        }
        locationHierarchy.setCountryCode(tuple.get(locationHierarchyAlias.concat(LOCATION_HIERARCHY_COUNTRY_CODE), String.class));
        if (tupleHasLocationId(tuple, prefixAlias, STATE)) {
            locationHierarchy.setState(mapTupleToLocation(tuple, prefixAlias, STATE));
        }
        locationHierarchy.setStateCode(tuple.get(locationHierarchyAlias.concat(LOCATION_HIERARCHY_STATE_CODE), String.class));
        if (tupleHasLocationId(tuple, prefixAlias, CITY)) {
            locationHierarchy.setCity(mapTupleToLocation(tuple, prefixAlias, CITY));
        }
        locationHierarchy.setCityCode(tuple.get(locationHierarchyAlias.concat(LOCATION_HIERARCHY_CITY_CODE), String.class));
        if (tupleHasLocationId(tuple, prefixAlias, FACILITY)) {
            locationHierarchy.setFacility(mapTupleToLocation(tuple, prefixAlias, FACILITY));
        }
        locationHierarchy.setFacilityCode(tuple.get(locationHierarchyAlias.concat(LOCATION_HIERARCHY_FACILITY_CODE), String.class));
        locationHierarchy.setFacilityLocationCode(tuple.get(locationHierarchyAlias.concat(LOCATION_HIERARCHY_FACILITY_LOCATION_CODE), String.class));

        return locationHierarchy;
    }

    private LocationEntity mapTupleToLocation(Tuple tuple, String prefixAlias, String locationType) {
        String locationPrefixAlias = prefixAlias.concat(locationType);
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setId(tuple.get(locationPrefixAlias.concat(ID), String.class));
        locationEntity.setName(tuple.get(locationPrefixAlias.concat(NAME), String.class));
        locationEntity.setExternalId(tuple.get(locationPrefixAlias.concat(EXTERNAL_ID), String.class));
        return locationEntity;
    }

    private boolean tupleHasLocationId(Tuple tuple, String prefixAlias, String locationType) {
        String locationPrefixAlias = prefixAlias.concat(locationType);
        return StringUtils.isNotBlank(tuple.get(locationPrefixAlias.concat(ID), String.class));
    }
}
