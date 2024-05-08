package com.quincus.shipment.impl.repository.specification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.AirlineFilter;
import com.quincus.shipment.api.filter.ShipmentLocationFilter;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity_;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity_;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.OrderEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
public class ShipmentSpecification extends BaseSpecification<ShipmentEntity> {
    private static final String LOWER_SQL_FUNCTION = "LOWER";
    private static final String JSON_CONTAINS_SQL_FUNCTION = "JSON_CONTAINS";
    private static final String JSON_WILD_CARD = "$";
    private static final String ADD_CUSTOMERS_PREDICATE = "addCustomersPredicate";
    private static final String ADD_SERVICE_TYPES_PREDICATE = "addServiceTypesPredicate";
    private static final String ADD_EXCLUDE_KEYS_PREDICATE = "addExcludeKeysPredicate";
    private static final String ADD_JOURNEY_PREDICATE = "addJourneyPredicate";
    private static final String ADD_KEYS_PREDICATE = "addKeysPredicate";
    private static final String ADD_COST_KEYS_PREDICATE = "addCostKeysPredicate";
    private static final String ADD_AIRLINE_KEYS = "addAirlineKeys";
    private static final String ADD_BOOKING_DATE_PREDICATE = "addBookingDatePredicate";
    private static final String ADD_FACILITY_PREDICATE = "addFacilityPredicate";
    private static final String ADD_FACILITY_LOCATIONS_PREDICATE = "addFacilityLocationsPredicate";
    private static final String ADD_ETA_STATUS_PREDICATE = "addEtaStatusPredicate";
    private static final String ADD_ORGANIZATION_PREDICATE = "addOrganizationPredicate";
    private static final String ADD_USER_PARTNERS_PREDICATE = "addUserPartnersPredicate";
    private static final String ADD_LOCATION_HIERARCHY_PREDICATE_ORIGIN = "addLocationHierarchyPredicateOrigin";
    private static final String ADD_LOCATION_PREDICATE_ORIGIN = "addLocationPredicateOrigin";
    private static final String ADD_LOCATION_HIERARCHY_PREDICATE_DESTINATION = "addLocationHierarchyPredicateDestination";
    private static final String ADD_LOCATION_PREDICATE_DESTINATION = "addLocationPredicateDestination";
    private static final String ADD_ORDER_PREDICATE = "addOrderPredicate";
    private static final String ADD_USER_LOCATION_COVERAGE_PREDICATE = "addUserLocationCoveragePredicate";
    private static final String ADD_NOT_DELETED_PREDICATE = "addNotDeletedPredicate";
    private static final String ADD_NOT_CANCELLED_PREDICATE = "addNotCancelledPredicate";
    private static final String ADD_SHIPMENT_AND_PACKAGE_JOURNEY_ALERT_PREDICATE = "addShipmentAndPackageJourneyAlertPredicate";
    private final transient ShipmentCriteria shipmentCriteria;
    private final ObjectMapper objectMapper;
    private final transient List<ShipmentLocationCoveragePredicate> shipmentCoverageRules;

    @Override
    public Predicate toPredicate(@NonNull Root<ShipmentEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        addOrganizationPredicate(root, criteriaBuilder, shipmentCriteria.getOrganization());
        addOrderPredicate(root, criteriaBuilder, shipmentCriteria.getOrder());
        addCustomersPredicate(root, shipmentCriteria.getCustomer());
        addEtaStatusPredicate(root, shipmentCriteria.getEtaStatus());
        addJourneyPredicate(root, shipmentCriteria.getJourneyStatus(), criteriaBuilder);
        addAirlineKeys(root, shipmentCriteria.getAirlineKeys(), criteriaBuilder);
        addKeysPredicate(root, shipmentCriteria.getKeys(), criteriaBuilder);
        addCostKeysPredicate(root, shipmentCriteria.getCostKeys(), criteriaBuilder);
        addServiceTypesPredicate(root, shipmentCriteria.getServiceType());
        addExcludeKeysPredicate(root, shipmentCriteria.getExcludeKeys(), criteriaBuilder);
        addLocationHierarchyPredicates(root, shipmentCriteria, criteriaBuilder);
        addBookingDatePredicate(root, shipmentCriteria.getBookingDateFrom(), shipmentCriteria.getBookingDateTo(), criteriaBuilder);
        addUserLocationCoveragePredicate(root, shipmentCriteria.getUserLocationCoverageIdsByType(), criteriaBuilder);
        addUserPartnersPredicate(root, shipmentCriteria.getPartnerId(), shipmentCriteria.getUserPartners(), criteriaBuilder);
        addShipmentAndPackageJourneyAlertPredicate(root, shipmentCriteria.getAlert(), criteriaBuilder);
        addExclusionPredicateShipmentDeleted(root, criteriaBuilder);
        //addExclusionPredicateShipmentCancelled(root, criteriaBuilder); //TODO: confirm w/ Su if cancelled shipments are not shown in Export
        return buildPredicate(query, criteriaBuilder);
    }

    public Pageable buildPageable() {
        return shipmentCriteria.pageRequest();
    }

    private void addLocationHierarchyPredicates(Root<ShipmentEntity> root, ShipmentCriteria shipmentCriteria, CriteriaBuilder criteriaBuilder) {
        if (ArrayUtils.isNotEmpty(shipmentCriteria.getOrigin())) {
            log.debug("Using origin location hierarchy IDs instead of performing a location sub query");
            addLocationHierarchyPredicate(ADD_LOCATION_HIERARCHY_PREDICATE_ORIGIN, root, criteriaBuilder, shipmentCriteria.getOrigin(), ShipmentEntity_.ORIGIN);
        } else {
            addLocationPredicate(ADD_LOCATION_PREDICATE_ORIGIN, root, criteriaBuilder, shipmentCriteria.getOriginLocations(), ShipmentEntity_.ORIGIN);
        }
        if (ArrayUtils.isNotEmpty(shipmentCriteria.getDestination())) {
            log.debug("Using destination location hierarchy IDs instead of performing a location sub query");
            addLocationHierarchyPredicate(ADD_LOCATION_HIERARCHY_PREDICATE_DESTINATION, root, criteriaBuilder, shipmentCriteria.getDestination(), ShipmentEntity_.DESTINATION);
        } else {
            addLocationPredicate(ADD_LOCATION_PREDICATE_DESTINATION, root, criteriaBuilder, shipmentCriteria.getDestinationLocations(), ShipmentEntity_.DESTINATION);
        }
        if (ArrayUtils.isNotEmpty(shipmentCriteria.getFacilities())) {
            log.debug("Using facility location hierarchy IDs instead of performing a location sub query.");
            addFacilityPredicate(root, shipmentCriteria.getFacilities(), criteriaBuilder);
        } else {
            addFacilityLocationsPredicate(root, shipmentCriteria.getFacilityLocations(), criteriaBuilder);
        }
    }

    private void addUserPartnersPredicate(Root<ShipmentEntity> root, String partnerId, List<String> userPartners,
                                          CriteriaBuilder criteriaBuilder) {
        Predicate partnersPredicate;
        if (partnerId != null) {
            partnersPredicate = createPartnersExclusivePredicate(root, partnerId, userPartners);
        } else {
            partnersPredicate = createPartnerPredicate(root, userPartners, criteriaBuilder);
        }
        addPredicate(ADD_USER_PARTNERS_PREDICATE, partnersPredicate);
    }

    private Predicate createPartnerPredicate(Root<ShipmentEntity> root, List<String> userPartners,
                                             CriteriaBuilder criteriaBuilder) {
        Predicate noPartnerPredicate = criteriaBuilder.isNull(root.get(ShipmentEntity_.partnerId));
        if (CollectionUtils.isEmpty(userPartners)) {
            // UserPartners is Empty: retrieve all shipments with no associated partners
            return noPartnerPredicate;
        }
        // UserPartners is not empty: retrieve all shipments with no associated partners and particular partner
        return criteriaBuilder.or(root.get(ShipmentEntity_.partnerId).in(userPartners), noPartnerPredicate);
    }

    private Predicate createPartnersExclusivePredicate(Root<ShipmentEntity> root, String partnerId,
                                                       List<String> userPartners) {
        List<String> partnersList = new ArrayList<>();
        partnersList.add(partnerId);
        if (CollectionUtils.isNotEmpty(userPartners)) {
            partnersList.addAll(userPartners);
        }
        return root.get(ShipmentEntity_.partnerId).in(partnersList);
    }

    private void addCustomersPredicate(Root<ShipmentEntity> root, Customer[] customers) {
        if (ArrayUtils.isNotEmpty(customers)) {
            List<String> customerIds = Arrays.stream(customers)
                    .map(Customer::getId)
                    .toList();
            addPredicate(ADD_CUSTOMERS_PREDICATE, root.get(ShipmentEntity_.CUSTOMER).get(BaseEntity_.ID).in(customerIds));
        }
    }

    private void addServiceTypesPredicate(Root<ShipmentEntity> root, ServiceType[] serviceTypes) {
        if (ArrayUtils.isNotEmpty(serviceTypes)) {
            List<String> serviceTypeIds = Arrays.stream(serviceTypes)
                    .map(ServiceType::getId)
                    .toList();
            addPredicate(ADD_SERVICE_TYPES_PREDICATE, root.get(ShipmentEntity_.SERVICE_TYPE).get(BaseEntity_.ID).in(serviceTypeIds));
        }
    }

    private void addExcludeKeysPredicate(Root<ShipmentEntity> root, String[] excludeShipmentIds, CriteriaBuilder criteriaBuilder) {
        if (ArrayUtils.isNotEmpty(excludeShipmentIds)) {
            Predicate predicate = root.get(ShipmentEntity_.SHIPMENT_TRACKING_ID).in(excludeShipmentIds);
            addPredicate(ADD_EXCLUDE_KEYS_PREDICATE, criteriaBuilder.not(predicate));
        }
    }

    private void addLocationHierarchyPredicate(String key, Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder, String[] locationHierarchies, String field) {
        if (ArrayUtils.isNotEmpty(locationHierarchies)) {
            Predicate joinedPredicate = criteriaBuilder.and(root.join(field).join(AddressEntity_.LOCATION_HIERARCHY).get(BaseEntity_.ID).in(locationHierarchies));
            addPredicate(key, joinedPredicate);
        }
    }

    private void addLocationPredicate(String key, Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder, ShipmentLocationFilter shipmentLocationFilter, String field) {
        if (shipmentLocationFilter != null && (ArrayUtils.isNotEmpty(shipmentLocationFilter.getCityIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getCountryIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getStateIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getFacilityIds()))) {
            Subquery<LocationHierarchyEntity> subQuery = getLocationHierarchySubQuery(shipmentLocationFilter, criteriaBuilder);
            Predicate joinedPredicate = criteriaBuilder.and(root.join(field).join(AddressEntity_.LOCATION_HIERARCHY).get(BaseEntity_.ID).in(subQuery));
            addPredicate(key, joinedPredicate);
        }
    }

    private void addJourneyPredicate(Root<ShipmentEntity> root, JourneyStatus journeyStatus, CriteriaBuilder criteriaBuilder) {
        if (journeyStatus != null) {
            Predicate joinedPredicate = criteriaBuilder.and(criteriaBuilder.equal(root.join(ShipmentEntity_.shipmentJourney).get(ShipmentJourneyEntity_.status), journeyStatus));
            addPredicate(ADD_JOURNEY_PREDICATE, joinedPredicate);
        }
    }

    private void addKeysPredicate(Root<ShipmentEntity> root, String[] keys, CriteriaBuilder criteriaBuilder) {
        Set<String> lowerCaseKeys = keys != null ? Arrays.stream(keys).filter(StringUtils::isNotBlank).map(String::toLowerCase).collect(Collectors.toSet()) : null;

        if (CollectionUtils.isNotEmpty(lowerCaseKeys)) {
            Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> pjs = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments);

            List<Predicate> keysPredicates = new ArrayList<>();

            Predicate flightNumbersAndMasterWayBillsPredicate = criteriaBuilder.or(
                    criteriaBuilder.lower(pjs.get(PackageJourneySegmentEntity_.flightNumber)).in(lowerCaseKeys),
                    criteriaBuilder.lower(pjs.get(PackageJourneySegmentEntity_.masterWaybill)).in(lowerCaseKeys));

            Predicate trackingIdsPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.shipmentTrackingId)).in(lowerCaseKeys);
            Predicate internalOrderIdPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.internalOrderId)).in(lowerCaseKeys);
            Predicate externalOrderIdPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.externalOrderId)).in(lowerCaseKeys);
            Predicate customerOrderIdPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.customerOrderId)).in(lowerCaseKeys);

            keysPredicates.add(flightNumbersAndMasterWayBillsPredicate);
            keysPredicates.add(trackingIdsPredicate);
            keysPredicates.add(internalOrderIdPredicate);
            keysPredicates.add(externalOrderIdPredicate);
            keysPredicates.add(customerOrderIdPredicate);

            Predicate combinedPredicates = criteriaBuilder.or(keysPredicates.toArray(new Predicate[0]));

            addPredicate(ADD_KEYS_PREDICATE, combinedPredicates);
        }
    }

    // TODO: keeping this method for future usage
    private Predicate createJSONContainsPredicate(String value, CriteriaBuilder criteriaBuilder) {
        Expression<Boolean> expression = criteriaBuilder.function(
                JSON_CONTAINS_SQL_FUNCTION, Boolean.class,
                criteriaBuilder.function(LOWER_SQL_FUNCTION, String.class, criteriaBuilder.literal(convertValueAsJsonString(value))),
                criteriaBuilder.literal(JSON_WILD_CARD));
        return criteriaBuilder.isTrue(expression);
    }

    private String convertValueAsJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.debug(String.format("Unable to convert value: `%s` to jsonString ", value));
        }
        return null;
    }

    private void addCostKeysPredicate(Root<ShipmentEntity> root, String[] keys, CriteriaBuilder criteriaBuilder) {

        List<String> lowerCaseKeys = keys != null ? Arrays.stream(keys).filter(StringUtils::isNotBlank).map(String::toLowerCase).toList() : null;

        if (CollectionUtils.isNotEmpty(lowerCaseKeys)) {
            Predicate trackingIdsPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.shipmentTrackingId)).in(lowerCaseKeys);
            Predicate orderIdLabelPredicate = criteriaBuilder.lower(root.get(ShipmentEntity_.order).get(OrderEntity_.orderIdLabel)).in(lowerCaseKeys);
            Predicate combinedPredicates = criteriaBuilder.or(orderIdLabelPredicate, trackingIdsPredicate);
            addPredicate(ADD_COST_KEYS_PREDICATE, combinedPredicates);
        }
    }

    private void addAirlineKeys(Root<ShipmentEntity> root, AirlineFilter[] airlineKeys, CriteriaBuilder criteriaBuilder) {
        if (ArrayUtils.isNotEmpty(airlineKeys)) {
            Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> pjs = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments);

            List<Predicate> predicates = Arrays.stream(airlineKeys)
                    .map(airlineFilter -> createPredicateForAirlineFlightFilter(airlineFilter, pjs, criteriaBuilder))
                    .toList();

            addPredicate(ADD_AIRLINE_KEYS, criteriaBuilder.or(predicates.toArray(new Predicate[0])));
        }
    }

    private Predicate createPredicateForAirlineFlightFilter(AirlineFilter airlineFilter, Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> pjs, CriteriaBuilder criteriaBuilder) {
        Predicate airlinePredicate = criteriaBuilder.equal(pjs.get(PackageJourneySegmentEntity_.airline), airlineFilter.getAirlineName());

        if (CollectionUtils.isEmpty(airlineFilter.getFlightNumbers())) {
            return airlinePredicate;
        }

        Predicate flightNumbersPredicate = pjs.get(PackageJourneySegmentEntity_.flightNumber).in(airlineFilter.getFlightNumbers());
        return criteriaBuilder.and(airlinePredicate, flightNumbersPredicate);
    }

    private void addBookingDatePredicate(Root<ShipmentEntity> root, Date bookingDateFromParam, Date bookingDateToParam, CriteriaBuilder criteriaBuilder) {
        Optional<Instant> bookingDateFrom = Optional.ofNullable(bookingDateFromParam).map(Date::toInstant);
        Optional<Instant> bookingDateTo = Optional.ofNullable(bookingDateToParam).map(Date::toInstant);
        if (bookingDateFrom.isPresent() && bookingDateTo.isPresent() && (bookingDateFrom.get().isBefore(bookingDateTo.get()) || bookingDateFrom.get().equals(bookingDateTo.get()))) {
            Predicate bookingDatePredicate = criteriaBuilder.between(root.get(BaseEntity_.CREATE_TIME), bookingDateFrom.get(), bookingDateTo.get().plus(1, ChronoUnit.DAYS));
            addPredicate(ADD_BOOKING_DATE_PREDICATE, bookingDatePredicate);
        }
    }


    private void addFacilityPredicate(Root<ShipmentEntity> root, String[] locationHierarchies, CriteriaBuilder criteriaBuilder) {
        if (ArrayUtils.isNotEmpty(locationHierarchies)) {
            Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> packageJourneySegments = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments);
            Predicate startFacility = packageJourneySegments.join(PackageJourneySegmentEntity_.startLocationHierarchy).get(BaseEntity_.ID).in(locationHierarchies);
            Predicate endFacility = packageJourneySegments.join(PackageJourneySegmentEntity_.endLocationHierarchy).get(BaseEntity_.ID).in(locationHierarchies);
            Predicate facilityPredicate = criteriaBuilder.or(startFacility, endFacility);
            addPredicate(ADD_FACILITY_PREDICATE, facilityPredicate);
        }
    }

    private void addFacilityLocationsPredicate(Root<ShipmentEntity> root, ShipmentLocationFilter shipmentLocationFilter, CriteriaBuilder criteriaBuilder) {
        if (shipmentLocationFilter != null && (ArrayUtils.isNotEmpty(shipmentLocationFilter.getCityIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getCountryIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getStateIds()) || ArrayUtils.isNotEmpty(shipmentLocationFilter.getFacilityIds()))) {
            Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> packageJourneySegments = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments);
            Subquery<LocationHierarchyEntity> subQuery = getLocationHierarchySubQuery(shipmentLocationFilter, criteriaBuilder);
            Predicate startFacility = packageJourneySegments.join(PackageJourneySegmentEntity_.startLocationHierarchy).in(subQuery);
            Predicate endFacility = packageJourneySegments.join(PackageJourneySegmentEntity_.endLocationHierarchy).in(subQuery);
            Predicate facilityPredicate = criteriaBuilder.or(startFacility, endFacility);
            addPredicate(ADD_FACILITY_LOCATIONS_PREDICATE, facilityPredicate);
        }
    }

    private void addEtaStatusPredicate(Root<ShipmentEntity> root, EtaStatus[] etaStatus) {
        if (ArrayUtils.isNotEmpty(etaStatus)) {
            addPredicate(ADD_ETA_STATUS_PREDICATE, root.get(ShipmentEntity_.ETA_STATUS).in(etaStatus));
        }
    }

    private void addOrganizationPredicate(Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder, Organization organization) {
        Optional.ofNullable(organization)
                .map(Organization::getId)
                .filter(StringUtils::isNotBlank)
                .ifPresent(organizationId -> addEqualsPredicate(root, ADD_ORGANIZATION_PREDICATE, criteriaBuilder, organizationId, MultiTenantEntity_.ORGANIZATION_ID));
    }

    private Predicate addLocationHierarchySubQueryPredicate(Root<LocationHierarchyEntity> locationHierarchyEntityRoot, String[] locationIds, SingularAttribute<LocationHierarchyEntity, LocationEntity> field) {
        return locationHierarchyEntityRoot.join(field, JoinType.LEFT).get(BaseEntity_.ID).in(locationIds);
    }

    private Subquery<LocationHierarchyEntity> getLocationHierarchySubQuery(final ShipmentLocationFilter shipmentLocationfilter, final CriteriaBuilder criteriaBuilder) {
        Subquery<LocationHierarchyEntity> subQuery = criteriaBuilder.createQuery().subquery(LocationHierarchyEntity.class);
        Root<LocationHierarchyEntity> locationHierarchyEntityRoot = subQuery.from(LocationHierarchyEntity.class);
        subQuery.select(locationHierarchyEntityRoot.get(BaseEntity_.ID));
        List<Predicate> subQueryPredicates = new ArrayList<>(Collections.emptyList());

        if (ArrayUtils.isNotEmpty(shipmentLocationfilter.getCountryIds())) {
            subQueryPredicates.add(addLocationHierarchySubQueryPredicate(locationHierarchyEntityRoot, shipmentLocationfilter.getCountryIds(), LocationHierarchyEntity_.country));
        }

        if (ArrayUtils.isNotEmpty(shipmentLocationfilter.getStateIds())) {
            subQueryPredicates.add(addLocationHierarchySubQueryPredicate(locationHierarchyEntityRoot, shipmentLocationfilter.getStateIds(), LocationHierarchyEntity_.state));
        }

        if (ArrayUtils.isNotEmpty(shipmentLocationfilter.getCityIds())) {
            subQueryPredicates.add(addLocationHierarchySubQueryPredicate(locationHierarchyEntityRoot, shipmentLocationfilter.getCityIds(), LocationHierarchyEntity_.city));
        }

        if (ArrayUtils.isNotEmpty(shipmentLocationfilter.getFacilityIds())) {
            subQueryPredicates.add(addLocationHierarchySubQueryPredicate(locationHierarchyEntityRoot, shipmentLocationfilter.getFacilityIds(), LocationHierarchyEntity_.facility));
        }

        subQuery.where(criteriaBuilder.or(subQueryPredicates.toArray(Predicate[]::new)));
        return subQuery;
    }

    private void addOrderPredicate(Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder, Order order) {
        Optional.ofNullable(order)
                .map(Order::getId)
                .filter(StringUtils::isNotBlank)
                .ifPresent(orderId -> addEqualsPredicate(root, ADD_ORDER_PREDICATE, criteriaBuilder, orderId, ShipmentEntity_.ORDER_ID));
    }

    private void addUserLocationCoveragePredicate(Root<ShipmentEntity> root, Map<LocationType, List<String>> userLocationCoverageByType, CriteriaBuilder criteriaBuilder) {
        if (MapUtils.isNotEmpty(userLocationCoverageByType)) {

            Join<ShipmentJourneyEntity, PackageJourneySegmentEntity> packageSegmentJoin = root.join(ShipmentEntity_.shipmentJourney)
                    .join(ShipmentJourneyEntity_.packageJourneySegments);

            Join<PackageJourneySegmentEntity, LocationHierarchyEntity> startLocHierarchyJoin = packageSegmentJoin
                    .join(PackageJourneySegmentEntity_.startLocationHierarchy);

            Join<PackageJourneySegmentEntity, LocationHierarchyEntity> endLocHierarchyJoin = packageSegmentJoin
                    .join(PackageJourneySegmentEntity_.endLocationHierarchy);

            List<Predicate> startLocationCoveragePredicate = shipmentCoverageRules.stream().map(r -> r.constructPredicate(startLocHierarchyJoin, criteriaBuilder, userLocationCoverageByType))
                    .filter(Objects::nonNull).toList();

            List<Predicate> endLocationCoveragePredicate = shipmentCoverageRules.stream().map(r -> r.constructPredicate(endLocHierarchyJoin, criteriaBuilder, userLocationCoverageByType))
                    .filter(Objects::nonNull).toList();

            List<Predicate> consolidatedLocationCoveragePredicate = Stream.concat(startLocationCoveragePredicate.stream(), endLocationCoveragePredicate.stream()).toList();

            if (CollectionUtils.isNotEmpty(consolidatedLocationCoveragePredicate)) {
                addPredicate(ADD_USER_LOCATION_COVERAGE_PREDICATE, criteriaBuilder.or(consolidatedLocationCoveragePredicate.toArray(new Predicate[0])));
            }
        }
    }

    private void addExclusionPredicateShipmentDeleted(Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder) {
        addPredicate(ADD_NOT_DELETED_PREDICATE, criteriaBuilder.not(criteriaBuilder.isTrue(root.get(ShipmentEntity_.DELETED))));
    }

    private void addExclusionPredicateShipmentCancelled(Root<ShipmentEntity> root, CriteriaBuilder criteriaBuilder) {
        addPredicate(ADD_NOT_DELETED_PREDICATE, criteriaBuilder.not(criteriaBuilder.isTrue(root.get(ShipmentEntity_.DELETED))));
        addNotEqualsPredicate(root, ADD_NOT_CANCELLED_PREDICATE, criteriaBuilder, ShipmentStatus.CANCELLED, ShipmentEntity_.STATUS);
    }

    private void addShipmentAndPackageJourneyAlertPredicate(Root<ShipmentEntity> root, String[] alerts, CriteriaBuilder criteriaBuilder) {
        if (ArrayUtils.isNotEmpty(alerts)) {
            Join<ShipmentJourneyEntity, AlertEntity> shipmentJourney = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.alerts, JoinType.LEFT);
            Join<PackageJourneySegmentEntity, AlertEntity> packageJourney = root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments).join(PackageJourneySegmentEntity_.alerts, JoinType.LEFT);

            Predicate shipmentJourneyAlert = shipmentJourney.get(AlertEntity_.shortMessage).in(alerts);
            Predicate packageJourneyAlert = packageJourney.get(AlertEntity_.shortMessage).in(alerts);
            Predicate alertPredicate = criteriaBuilder.or(shipmentJourneyAlert, packageJourneyAlert);

            addPredicate(ADD_SHIPMENT_AND_PACKAGE_JOURNEY_ALERT_PREDICATE, alertPredicate);
        }
    }
}
