package com.quincus.shipment.impl.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.LocationType;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.AirlineFilter;
import com.quincus.shipment.api.filter.ShipmentLocationFilter;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity_;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ShipmentSpecificationTest {
    @Mock
    private Path<Object> path;
    @Mock
    private Path<String> pathStr;
    @Mock
    private Path<JourneyStatus> pathJourneyStatus;
    @Mock
    private CriteriaQuery<Object> criteriaQuery;
    @Mock
    private Subquery subquery;
    @Mock
    private Join join;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Root<ShipmentEntity> root;
    @Mock
    private Root<LocationHierarchyEntity> locationHierarchyEntityRoot;
    @Mock
    private ListJoin<ShipmentJourneyEntity, PackageJourneySegmentEntity> listJoinJourney;
    @Mock
    private ListJoin<PackageJourneySegmentEntity, LocationHierarchyEntity> listJoinLocationHierarchySegment;
    @Mock
    private ListJoin<ShipmentJourneyEntity, AlertEntity> listJoinShipmenJourneytAlert;
    @Mock
    private ListJoin<PackageJourneySegmentEntity, AlertEntity> listJoinPackageJourneyAlert;
    @Mock
    private ListJoin<ShipmentJourneyEntity, PackageJourneySegmentEntity> listJoinShipmentPackageJourney;

    @Mock
    private ObjectMapper objectMapper;
    private ShipmentSpecification classUnderTest;
    @Mock
    private List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;

    @Test
    @DisplayName("GIVEN all filters without keys  WHEN building predicates THEN get valid criteria size")
    void givenAllFiltersWithoutKeys_WhenToPredicates_ThenGetValidCriteriaSize() {

        ShipmentCriteria criteria = buildShipmentCriteria();
        criteria.setKeys(null);
        classUnderTest = new ShipmentSpecification(criteria, objectMapper, shipmentLocationCoveragePredicates);

        mockCallsForAllFiltersExceptLocationFilters();
        mockCallsWhenOriginOrDestinationOrFacilitiesArePresent();
        mockCallsForAirFlightsPredicate();

        classUnderTest.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertThat(classUnderTest.getPredicates()).hasSize(15);
    }

    @Test
    @DisplayName("GIVEN all filters without origin, destination, facilities and keys WHEN building predicates THEN get valid criteria size")
    void givenAllFilterWithoutOriginAndDestinationAndFacilitiesAndKeys_WhenToPredicates_ThenGetValidCriteriaSize() {
        ShipmentCriteria criteria = buildShipmentCriteria();
        criteria.setKeys(null);
        criteria.setOrigin(null);
        criteria.setDestination(null);
        criteria.setFacilities(null);
        criteria.setUserPartners(null);
        classUnderTest = new ShipmentSpecification(criteria, objectMapper, shipmentLocationCoveragePredicates);

        mockCallsForAllFiltersExceptLocationFilters();
        mockCallsWhenOriginAndDestinationAndFacilitiesAreNotPresent();
        mockCallsForAirFlightsPredicate();

        classUnderTest.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertThat(classUnderTest.getPredicates()).hasSize(15);
    }

    @Test
    @DisplayName("GIVEN all filters WHEN building predicates THEN get valid criteria size")
    void givenAllFilters_WhenToPredicates_ThenGetValidCriteriaSize() {
        ShipmentCriteria criteria = buildShipmentCriteria();
        classUnderTest = new ShipmentSpecification(criteria, objectMapper, shipmentLocationCoveragePredicates);

        mockCallsForAllFiltersExceptLocationFilters();
        mockCallsWhenOriginOrDestinationOrFacilitiesArePresent();
        mockCallsForKeysPredicate();
        mockCallsForAirFlightsPredicate();

        classUnderTest.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertThat(classUnderTest.getPredicates()).hasSize(16);
    }

    //TODO: add unit test to confirm partner predicate

    @Test
    @DisplayName("GIVEN filter WHEN build pageable THEN return valid pageable")
    void givenFilter_WhenBuildPageable_ThenGetValidCriteriaSize() {
        ShipmentCriteria criteria = buildShipmentCriteria();
        classUnderTest = new ShipmentSpecification(criteria, objectMapper, shipmentLocationCoveragePredicates);
        Pageable shipmentSpecificationPageable = classUnderTest.buildPageable();

        assertThat(shipmentSpecificationPageable.getPageNumber()).isEqualTo(criteria.getPage() - 1);
        assertThat(shipmentSpecificationPageable.getPageSize()).isEqualTo(criteria.getPerPage());
        assertThat(shipmentSpecificationPageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, ShipmentEntity_.CREATE_TIME));
    }

    private void mockCallsWhenOriginAndDestinationAndFacilitiesAreNotPresent() {
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.subquery(any())).thenReturn(subquery);
        when(subquery.from(LocationHierarchyEntity.class)).thenReturn(locationHierarchyEntityRoot);
        when(locationHierarchyEntityRoot.join((SingularAttribute<? super LocationHierarchyEntity, Object>) any(), any())).thenReturn(join);
        when(join.get(anyString())).thenReturn(path);
    }

    private void mockCallsForAllFiltersExceptLocationFilters() {
        when(root.get(ShipmentEntity_.SHIPMENT_TRACKING_ID)).thenReturn(path);
        when(root.join(ShipmentEntity_.shipmentJourney)).thenReturn(join);
        when(root.join(ShipmentEntity_.shipmentJourney).get(ShipmentJourneyEntity_.status)).thenReturn(pathJourneyStatus);

        when(root.get(ShipmentEntity_.ETA_STATUS)).thenReturn(path);
        when(root.get(ShipmentEntity_.CUSTOMER)).thenReturn(path);

        when(root.join(anyString())).thenReturn(join);
        when(join.join(anyString())).thenReturn(join);

        when(root.join(ShipmentEntity_.shipmentJourney)).thenReturn(join);
        when(root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments)).thenReturn(listJoinJourney);
        when(listJoinJourney.join(PackageJourneySegmentEntity_.flightNumber)).thenReturn(join);
        when(listJoinJourney.join(PackageJourneySegmentEntity_.masterWaybill)).thenReturn(join);
        when(listJoinJourney.join(PackageJourneySegmentEntity_.alerts, JoinType.LEFT)).thenReturn(listJoinPackageJourneyAlert);
        when(root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.alerts, JoinType.LEFT)).thenReturn(listJoinShipmenJourneytAlert);


        when(root.get(ShipmentEntity_.SERVICE_TYPE)).thenReturn(path);
        when(root.get(ShipmentEntity_.SERVICE_TYPE).get("id")).thenReturn(path);
        when(root.get(ShipmentEntity_.ORDER_ID)).thenReturn(path);
        when(root.get(ShipmentEntity_.ORGANIZATION_ID)).thenReturn(path);

        when(root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments)
                .join(PackageJourneySegmentEntity_.startLocationHierarchy)).thenReturn(listJoinLocationHierarchySegment);

        when(root.join(ShipmentEntity_.shipmentJourney).join(ShipmentJourneyEntity_.packageJourneySegments)
                .join(PackageJourneySegmentEntity_.endLocationHierarchy)).thenReturn(listJoinLocationHierarchySegment);

        when(listJoinShipmenJourneytAlert.get(AlertEntity_.shortMessage)).thenReturn(join);
        when(listJoinPackageJourneyAlert.get(AlertEntity_.shortMessage)).thenReturn(join);
    }

    private void mockCallsWhenOriginOrDestinationOrFacilitiesArePresent() {
        when(root.join(anyString()).join(AddressEntity_.LOCATION_HIERARCHY).get("id")).thenReturn(path);
    }

    private void mockCallsForKeysPredicate() {
        when(root.get(ShipmentEntity_.shipmentTrackingId)).thenReturn(pathStr);
        when(root.get(ShipmentEntity_.shipmentTrackingId)).thenReturn(pathStr);
        when(root.get(ShipmentEntity_.partnerId)).thenReturn(pathStr);
        when(listJoinJourney.join(PackageJourneySegmentEntity_.flightNumber)).thenReturn(join);
        when(listJoinJourney.get(PackageJourneySegmentEntity_.flightNumber)).thenReturn(pathStr);
        when(listJoinJourney.join(PackageJourneySegmentEntity_.masterWaybill)).thenReturn(join);
        when(listJoinJourney.get(PackageJourneySegmentEntity_.masterWaybill)).thenReturn(pathStr);
        when(criteriaBuilder.lower(any())).thenReturn(pathStr);
    }

    private void mockCallsForAirFlightsPredicate() {
        when(listJoinJourney.join(PackageJourneySegmentEntity_.flightNumber)).thenReturn(join);
        when(listJoinJourney.get(PackageJourneySegmentEntity_.flightNumber)).thenReturn(pathStr);
    }

    private ShipmentCriteria buildShipmentCriteria() {
        ShipmentCriteria criteria = new ShipmentCriteria();
        int pageNumber = 3;
        int size = 1;
        criteria.setPage(pageNumber);
        criteria.setPerPage(size);
        criteria.setAlert(new String[]{"Blank mandatory field", "Time overlaps across segments"});

        Organization organization = new Organization();
        organization.setId("SINGLE-OsRG-ID-ONE");
        criteria.setOrganization(organization);

        Customer customer = new Customer();
        customer.setId("4028c4ee84a30bdb0184a30d315e004a");
        criteria.setCustomer(new Customer[]{customer});

        ServiceType serviceType = new ServiceType();
        serviceType.setId("4028c4ee84a30bdb0184a30d326a0060");
        criteria.setServiceType(new ServiceType[]{serviceType});

        Order order = new Order();
        order.setId("ORDER-MMMM-11");
        criteria.setOrder(order);

        Facility facility = new Facility();
        facility.setId("4028c4ee84a30bdb0184a34ccef30c92");
        criteria.setFacilities(new String[]{"facility-1", "facility-2"});

        criteria.setEtaStatus(new EtaStatus[]{EtaStatus.ON_TIME});
        criteria.setOrigin(new String[]{"4028c4ee84a30bdb0184a30d2ec60035"});
        criteria.setDestination(new String[]{"4028c4ee84a30bdb0184a30d30be0045"});
        criteria.setKeys(new String[]{"shipmentTrackingId", "orderLabel", "externalOrderId", "internalOrderId", "customerOrderId"});
        criteria.setExcludeKeys(new String[]{"shipmentTrackingId"});
        criteria.setJourneyStatus(JourneyStatus.ACTIVE);


        ShipmentLocationFilter locationFilter = new ShipmentLocationFilter();
        locationFilter.setCityIds(new String[]{"city-1", "city-2"});
        locationFilter.setCountryIds(new String[]{"country-1", "country-2"});
        locationFilter.setStateIds(new String[]{"state-1", "state-2"});
        locationFilter.setFacilityIds(new String[]{"facility-1", "facility-2"});

        criteria.setOriginLocations(locationFilter);
        criteria.setDestinationLocations(locationFilter);
        criteria.setFacilityLocations(locationFilter);

        Date dateNow = new Date();
        criteria.setBookingDateFrom(DateUtils.addDays(dateNow, -2));
        criteria.setBookingDateTo(dateNow);

        AirlineFilter airlineFilter = new AirlineFilter();
        airlineFilter.setAirlineName("Singapore Airlines");
        airlineFilter.setFlightNumbers(List.of("ML1324"));

        criteria.setAirlineKeys(new AirlineFilter[]{airlineFilter});

        Map<LocationType, List<String>> userLocationCoveragesByType = new HashMap<>();
        userLocationCoveragesByType.put(LocationType.COUNTRY, Arrays.asList("1", "2"));
        userLocationCoveragesByType.put(LocationType.CITY, Arrays.asList("3", "4"));
        criteria.setUserLocationCoverageIdsByType(userLocationCoveragesByType);

        return criteria;
    }
}
