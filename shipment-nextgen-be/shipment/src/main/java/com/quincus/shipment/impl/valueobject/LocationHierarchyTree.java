package com.quincus.shipment.impl.valueobject;

import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LocationHierarchyTree {

    private LocationType type;
    private String code;
    private String name;
    private String description;
    private String externalId;
    private String timezone;
    private String organizationId;
    private String id;
    private String parentCode;
    private LocationHierarchyTree parent;
    private List<LocationHierarchyTree> children;

    public LocationHierarchyTree(LocationEntity location,
                                 String parentCode) {
        this.children = new ArrayList<>();
        this.type = location.getType();
        this.code = location.getCode();
        this.id = location.getId();
        this.description = location.getDescription();
        this.externalId = location.getExternalId();
        this.organizationId = location.getOrganizationId();
        this.name = location.getName();
        this.timezone = location.getTimezone();
        this.parentCode = parentCode;
    }

    /**
     * Converts a list of Tuple to a List of LocationHierarchyTree
     *
     * @param locationList list of tuples
     * @param level        list of location hierarchy trees
     *
     * @return List<LocationHierarchyTree>
     */
    public static List<LocationHierarchyTree> parseTreeList(List<Tuple> locationList, int level) {
        if (CollectionUtils.isEmpty(locationList)) {
            return Collections.emptyList();
        }
        final Map<String, LocationHierarchyTree> countriesLocationMap = new LinkedHashMap<>();
        final Map<String, LocationHierarchyTree> locationMap = new HashMap<>();
        locationList.forEach(currentTuple -> {
            LocationHierarchyEntity currentLocationHierarchyEntity = new LocationHierarchyEntity();
            if (level >= LocationType.FACILITY.value()) {
                currentLocationHierarchyEntity.setFacility(createLocationEntityFromTuple(currentTuple, LocationHierarchyEntity_.FACILITY));
            }
            if (level >= LocationType.CITY.value()) {
                currentLocationHierarchyEntity.setCity(createLocationEntityFromTuple(currentTuple, LocationHierarchyEntity_.CITY));
            }
            if (level >= LocationType.STATE.value()) {
                currentLocationHierarchyEntity.setState(createLocationEntityFromTuple(currentTuple, LocationHierarchyEntity_.STATE));
            }
            currentLocationHierarchyEntity.setCountry(createLocationEntityFromTuple(currentTuple, LocationHierarchyEntity_.COUNTRY));
            assignLocationHierarchyTreeNode(level, countriesLocationMap, locationMap, currentLocationHierarchyEntity);
        });
        return countriesLocationMap.values().stream().toList();
    }

    private static LocationEntity createLocationEntityFromTuple(Tuple currentTuple, String locationType) {
        LocationEntity entity = new LocationEntity();
        entity.setId(currentTuple.get(locationType.concat("Id"), String.class));
        entity.setType(currentTuple.get(locationType.concat("Type"), LocationType.class));
        entity.setDescription(currentTuple.get(locationType.concat("Description"), String.class));
        entity.setCode(currentTuple.get(locationType.concat("Code"), String.class));
        entity.setName(currentTuple.get(locationType.concat("Name"), String.class));
        entity.setExternalId(currentTuple.get(locationType.concat("ExternalId"), String.class));
        entity.setTimezone(currentTuple.get(locationType.concat("Timezone"), String.class));
        entity.setOrganizationId(currentTuple.get(locationType.concat("OrganizationId"), String.class));
        return entity;
    }

    private static void assignLocationHierarchyTreeNode(int level, Map<String, LocationHierarchyTree> countriesLocationMap, Map<String, LocationHierarchyTree> locationMap, LocationHierarchyEntity currentLocationHierarchyEntity) {
        LocationHierarchyTree candidateLocationHierarchyTreeChildNode = null;
        //create facility node
        if (level >= LocationType.FACILITY.value()) {
            candidateLocationHierarchyTreeChildNode = createOrUpdateLocationHierarchyTreeNode(locationMap, null, currentLocationHierarchyEntity.getFacility(), currentLocationHierarchyEntity.getCity().getId());
        }
        //create city node
        if (level >= LocationType.CITY.value()) {
            candidateLocationHierarchyTreeChildNode = createOrUpdateLocationHierarchyTreeNode(locationMap, candidateLocationHierarchyTreeChildNode, currentLocationHierarchyEntity.getCity(), currentLocationHierarchyEntity.getState().getId());
        }
        //create state node
        if (level >= LocationType.STATE.value()) {
            candidateLocationHierarchyTreeChildNode = createOrUpdateLocationHierarchyTreeNode(locationMap, candidateLocationHierarchyTreeChildNode, currentLocationHierarchyEntity.getState(), currentLocationHierarchyEntity.getCountry().getId());
        }
        //create root/country node
        createOrUpdateLocationHierarchyTreeNode(countriesLocationMap, candidateLocationHierarchyTreeChildNode, currentLocationHierarchyEntity.getCountry(), null);
    }

    private static LocationHierarchyTree createOrUpdateLocationHierarchyTreeNode(Map<String, LocationHierarchyTree> locationMap,
                                                                                 LocationHierarchyTree child,
                                                                                 LocationEntity locationEntity,
                                                                                 String parentCode) {
        if (locationEntity == null) return null;
        LocationHierarchyTree locationHierarchyTreeNode = locationMap.computeIfAbsent(locationEntity.getId(), k -> new LocationHierarchyTree(locationEntity, parentCode));
        locationHierarchyTreeNode.addChild(child);
        return locationHierarchyTreeNode;
    }

    private void addChild(LocationHierarchyTree child) {
        if (!this.children.contains(child) && child != null) {
            this.children.add(child);
        }
    }
}