package com.quincus.shipment.impl.repository.specification;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.NetworkLaneLocationFilter;
import com.quincus.shipment.impl.repository.criteria.NetworkLaneCriteria;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
public class NetworkLaneSpecification extends BaseSpecification<NetworkLaneEntity> {
    private static final String ADD_SERVICE_TYPES_IDS_PREDICATE = "addServiceTypeIdsPredicate";
    private static final String ADD_SERVICE_TYPES_NAMES_PREDICATE = "addServiceTypeNamesPredicate";
    private static final String ADD_ORIGIN_PREDICATE = "addOriginPredicate";
    private static final String ADD_ORIGIN_COUNTRY_EXTERNAL_PREDICATE = "addOriginCountryExtPredicate";
    private static final String ADD_ORIGIN_STATE_EXTERNAL_PREDICATE = "addOriginStateExtPredicate";
    private static final String ADD_ORIGIN_CITY_EXTERNAL_PREDICATE = "addOriginCityExtPredicate";
    private static final String ADD_ORIGIN_FACILITY_EXTERNAL_PREDICATE = "addOriginFacilityExtPredicate";
    private static final String ADD_DESTINATION_PREDICATE = "addDestinationPredicate";
    private static final String ADD_DESTINATION_COUNTRY_EXTERNAL_PREDICATE = "addDestinationCountryExtPredicate";
    private static final String ADD_DESTINATION_STATE_EXTERNAL_PREDICATE = "addDestinationStateExtPredicate";
    private static final String ADD_DESTINATION_CITY_EXTERNAL_PREDICATE = "addDestinationCityExtPredicate";
    private static final String ADD_DESTINATION_FACILITY_EXTERNAL_PREDICATE = "addDestinationFacilityExtPredicate";
    private static final String ADD_FACILITY_PREDICATE = "addFacilityPredicate";

    private final transient NetworkLaneCriteria networkLaneCriteria;

    @Override
    public Predicate toPredicate(@NonNull Root<NetworkLaneEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        addOrganizationPredicate(root, criteriaBuilder, networkLaneCriteria.getOrganizationId());
        addServiceTypesPredicate(root, networkLaneCriteria.getServiceTypes());
        addOriginOrPredicates(root, networkLaneCriteria.getOriginLocations(), criteriaBuilder);
        addOriginExternalIdAndPredicates(root, networkLaneCriteria.getOriginLocations());
        addDestinationOrPredicates(root, networkLaneCriteria.getDestinationLocations(), criteriaBuilder);
        addDestinationExternalIdAndPredicates(root, networkLaneCriteria.getDestinationLocations());
        addSegmentStartEndFacilityOrPredicates(root, networkLaneCriteria.getFacilityLocations(), criteriaBuilder);
        return buildPredicate(query, criteriaBuilder);
    }

    private void addServiceTypesPredicate(Root<NetworkLaneEntity> root, List<ServiceType> serviceTypes) {
        if (CollectionUtils.isEmpty(serviceTypes)) {
            return;
        }
        Join<NetworkLaneEntity, ServiceTypeEntity> networkLaneServiceTypeJoin = root.join(NetworkLaneEntity_.SERVICE_TYPE);
        List<String> serviceTypeIds =
                serviceTypes.stream().map(ServiceType::getId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isNotEmpty(serviceTypeIds)) {
            addPredicate(ADD_SERVICE_TYPES_IDS_PREDICATE, networkLaneServiceTypeJoin.get(BaseEntity_.ID).in(serviceTypeIds));
        }

        List<String> serviceTypeNames =
                serviceTypes.stream().map(ServiceType::getName).filter(Objects::nonNull).toList();
        if (CollectionUtils.isNotEmpty(serviceTypeNames)) {
            addPredicate(ADD_SERVICE_TYPES_NAMES_PREDICATE, networkLaneServiceTypeJoin.get(ServiceTypeEntity_.NAME).in(serviceTypeNames));
        }
    }

    private void addOriginOrPredicates(Root<NetworkLaneEntity> root, NetworkLaneLocationFilter originLocations, CriteriaBuilder criteriaBuilder) {
        if (originLocations == null || allLocationFiltersAreEmpty(originLocations)) {
            return;
        }
        List<Predicate> originPredicates = new ArrayList<>();
        Join<NetworkLaneEntity, LocationHierarchyEntity> networkLaneOriginJoin = root.join(NetworkLaneEntity_.ORIGIN);
        if (CollectionUtils.isNotEmpty(originLocations.getCountryIds())) {
            originPredicates.add(networkLaneOriginJoin.get(LocationHierarchyEntity_.COUNTRY_ID).in(originLocations.getCountryIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getStateIds())) {
            originPredicates.add(networkLaneOriginJoin.get(LocationHierarchyEntity_.STATE_ID).in(originLocations.getStateIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getCityIds())) {
            originPredicates.add(networkLaneOriginJoin.get(LocationHierarchyEntity_.CITY_ID).in(originLocations.getCityIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getFacilityIds())) {
            originPredicates.add(networkLaneOriginJoin.get(LocationHierarchyEntity_.FACILITY_ID).in(originLocations.getFacilityIds()));
        }
        if (CollectionUtils.isNotEmpty(originPredicates)) {
            addPredicate(ADD_ORIGIN_PREDICATE, criteriaBuilder.or(originPredicates.toArray(new Predicate[0])));
        }
    }

    private void addOriginExternalIdAndPredicates(Root<NetworkLaneEntity> root, NetworkLaneLocationFilter originLocations) {
        if (originLocations == null || allLocationExtIdFiltersAreEmpty(originLocations)) {
            return;
        }
        Join<NetworkLaneEntity, LocationHierarchyEntity> networkLaneOriginJoin = root.join(NetworkLaneEntity_.ORIGIN);
        if (CollectionUtils.isNotEmpty(originLocations.getCountryExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyCountryJoin = networkLaneOriginJoin.join(LocationHierarchyEntity_.COUNTRY);
            addPredicate(ADD_ORIGIN_COUNTRY_EXTERNAL_PREDICATE, locHierarchyCountryJoin.get(LocationEntity_.EXTERNAL_ID).in(originLocations.getCountryExtIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getStateExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyStateJoin = networkLaneOriginJoin.join(LocationHierarchyEntity_.STATE);
            addPredicate(ADD_ORIGIN_STATE_EXTERNAL_PREDICATE, locHierarchyStateJoin.get(LocationEntity_.EXTERNAL_ID).in(originLocations.getStateExtIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getCityExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyCityJoin = networkLaneOriginJoin.join(LocationHierarchyEntity_.CITY);
            addPredicate(ADD_ORIGIN_CITY_EXTERNAL_PREDICATE, locHierarchyCityJoin.get(LocationEntity_.EXTERNAL_ID).in(originLocations.getCityExtIds()));
        }
        if (CollectionUtils.isNotEmpty(originLocations.getFacilityExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyFacilityJoin = networkLaneOriginJoin.join(LocationHierarchyEntity_.FACILITY);
            addPredicate(ADD_ORIGIN_FACILITY_EXTERNAL_PREDICATE, locHierarchyFacilityJoin.get(LocationEntity_.EXTERNAL_ID).in(originLocations.getFacilityExtIds()));
        }
    }

    private void addDestinationOrPredicates(Root<NetworkLaneEntity> root, NetworkLaneLocationFilter destinationLocations, CriteriaBuilder criteriaBuilder) {
        if (destinationLocations == null || allLocationFiltersAreEmpty(destinationLocations)) {
            return;
        }
        List<Predicate> destinationPredicates = new ArrayList<>();
        Join<NetworkLaneEntity, LocationHierarchyEntity> networkLaneDestinationJoin = root.join(NetworkLaneEntity_.DESTINATION);
        if (CollectionUtils.isNotEmpty(destinationLocations.getCountryIds())) {
            destinationPredicates.add(networkLaneDestinationJoin.get(LocationHierarchyEntity_.COUNTRY_ID).in(destinationLocations.getCountryIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getStateIds())) {
            destinationPredicates.add(networkLaneDestinationJoin.get(LocationHierarchyEntity_.STATE_ID).in(destinationLocations.getStateIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getCityIds())) {
            destinationPredicates.add(networkLaneDestinationJoin.get(LocationHierarchyEntity_.CITY_ID).in(destinationLocations.getCityIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getFacilityIds())) {
            destinationPredicates.add(networkLaneDestinationJoin.get(LocationHierarchyEntity_.FACILITY_ID).in(destinationLocations.getFacilityIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationPredicates)) {
            addPredicate(ADD_DESTINATION_PREDICATE, criteriaBuilder.or(destinationPredicates.toArray(new Predicate[0])));
        }
    }

    private void addDestinationExternalIdAndPredicates(Root<NetworkLaneEntity> root, NetworkLaneLocationFilter destinationLocations) {
        if (destinationLocations == null || allLocationExtIdFiltersAreEmpty(destinationLocations)) {
            return;
        }
        Join<NetworkLaneEntity, LocationHierarchyEntity> networkLaneDestinationJoin = root.join(NetworkLaneEntity_.DESTINATION);
        if (CollectionUtils.isNotEmpty(destinationLocations.getCountryExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyCountryJoin = networkLaneDestinationJoin.join(LocationHierarchyEntity_.COUNTRY);
            addPredicate(ADD_DESTINATION_COUNTRY_EXTERNAL_PREDICATE, locHierarchyCountryJoin.get(LocationEntity_.EXTERNAL_ID).in(destinationLocations.getCountryExtIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getStateExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyStateJoin = networkLaneDestinationJoin.join(LocationHierarchyEntity_.STATE);
            addPredicate(ADD_DESTINATION_STATE_EXTERNAL_PREDICATE, locHierarchyStateJoin.get(LocationEntity_.EXTERNAL_ID).in(destinationLocations.getStateExtIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getCityExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyCityJoin = networkLaneDestinationJoin.join(LocationHierarchyEntity_.CITY);
            addPredicate(ADD_DESTINATION_CITY_EXTERNAL_PREDICATE, locHierarchyCityJoin.get(LocationEntity_.EXTERNAL_ID).in(destinationLocations.getCityExtIds()));
        }
        if (CollectionUtils.isNotEmpty(destinationLocations.getFacilityExtIds())) {
            Join<LocationHierarchyEntity, LocationEntity> locHierarchyFacilityJoin = networkLaneDestinationJoin.join(LocationHierarchyEntity_.FACILITY);
            addPredicate(ADD_DESTINATION_FACILITY_EXTERNAL_PREDICATE, locHierarchyFacilityJoin.get(LocationEntity_.EXTERNAL_ID).in(destinationLocations.getFacilityExtIds()));
        }
    }

    private void addSegmentStartEndFacilityOrPredicates(Root<NetworkLaneEntity> root, NetworkLaneLocationFilter facilityLocations, CriteriaBuilder criteriaBuilder) {
        if (facilityLocations == null || allLocationFiltersAreEmpty(facilityLocations)) {
            return;
        }
        List<Predicate> facilityPredicates = new ArrayList<>();
        Join<NetworkLaneEntity, NetworkLaneSegmentEntity> networkLaneDestinationJoin = root.join(NetworkLaneEntity_.NETWORK_LANE_SEGMENT_LIST);
        Join<NetworkLaneSegmentEntity, LocationHierarchyEntity> startLocationJoin = networkLaneDestinationJoin.join(NetworkLaneSegmentEntity_.START_LOCATION_HIERARCHY);
        Join<NetworkLaneSegmentEntity, LocationHierarchyEntity> endLocationJoin = networkLaneDestinationJoin.join(NetworkLaneSegmentEntity_.END_LOCATION_HIERARCHY);

        if (CollectionUtils.isNotEmpty(facilityLocations.getCountryIds())) {
            facilityPredicates.add(startLocationJoin.get(LocationHierarchyEntity_.COUNTRY_ID).in(facilityLocations.getCountryIds()));
            facilityPredicates.add(endLocationJoin.get(LocationHierarchyEntity_.COUNTRY_ID).in(facilityLocations.getCountryIds()));

        }
        if (CollectionUtils.isNotEmpty(facilityLocations.getStateIds())) {
            facilityPredicates.add(startLocationJoin.get(LocationHierarchyEntity_.STATE_ID).in(facilityLocations.getStateIds()));
            facilityPredicates.add(endLocationJoin.get(LocationHierarchyEntity_.STATE_ID).in(facilityLocations.getStateIds()));
        }
        if (CollectionUtils.isNotEmpty(facilityLocations.getCityIds())) {
            facilityPredicates.add(startLocationJoin.get(LocationHierarchyEntity_.CITY_ID).in(facilityLocations.getCityIds()));
            facilityPredicates.add(endLocationJoin.get(LocationHierarchyEntity_.CITY_ID).in(facilityLocations.getCityIds()));
        }
        if (CollectionUtils.isNotEmpty(facilityLocations.getFacilityIds())) {
            facilityPredicates.add(startLocationJoin.get(LocationHierarchyEntity_.FACILITY_ID).in(facilityLocations.getFacilityIds()));
            facilityPredicates.add(endLocationJoin.get(LocationHierarchyEntity_.FACILITY_ID).in(facilityLocations.getFacilityIds()));
        }
        if (CollectionUtils.isNotEmpty(facilityPredicates)) {
            addPredicate(ADD_FACILITY_PREDICATE, criteriaBuilder.or(facilityPredicates.toArray(new Predicate[0])));
        }
    }

    private void addOrganizationPredicate(Root<NetworkLaneEntity> root, CriteriaBuilder criteriaBuilder, String organizationId) {
        addEqualsPredicate(root, ADD_ORGANIZATION_PREDICATE, criteriaBuilder, organizationId, MultiTenantEntity_.ORGANIZATION_ID);
    }

    private boolean allLocationFiltersAreEmpty(NetworkLaneLocationFilter locationFilter) {
        return CollectionUtils.isEmpty(locationFilter.getCountryIds())
                && CollectionUtils.isEmpty(locationFilter.getStateIds())
                && CollectionUtils.isEmpty(locationFilter.getCityIds())
                && CollectionUtils.isEmpty(locationFilter.getFacilityIds());
    }

    private boolean allLocationExtIdFiltersAreEmpty(NetworkLaneLocationFilter locationFilter) {
        return CollectionUtils.isEmpty(locationFilter.getCountryExtIds())
                && CollectionUtils.isEmpty(locationFilter.getStateExtIds())
                && CollectionUtils.isEmpty(locationFilter.getCityExtIds())
                && CollectionUtils.isEmpty(locationFilter.getFacilityExtIds());
    }

    public Pageable buildPageable() {
        return networkLaneCriteria.pageRequest();
    }

}
