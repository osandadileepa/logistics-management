package com.quincus.shipment.impl.specification;

import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.filter.NetworkLaneLocationFilter;
import com.quincus.shipment.impl.repository.criteria.NetworkLaneCriteria;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.specification.NetworkLaneSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class NetworkLaneSpecificationTest {
    private static final List<String> EXPECTED_PREDICATE_KEYS = List.of("addOrganizationPredicate"
            , "addServiceTypeIdsPredicate", "addServiceTypeNamesPredicate", "addOriginPredicate"
            , "addOriginCountryExtPredicate", "addOriginStateExtPredicate", "addOriginCityExtPredicate"
            , "addOriginFacilityExtPredicate", "addDestinationPredicate", "addDestinationCountryExtPredicate"
            , "addDestinationStateExtPredicate", "addDestinationCityExtPredicate", "addDestinationFacilityExtPredicate"
            , "addFacilityPredicate");

    private NetworkLaneSpecification specification;
    @Mock
    private NetworkLaneCriteria networkLaneCriteria;
    @Mock
    private Root<NetworkLaneEntity> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder criteriaBuilder;

    @BeforeEach
    void setUp() {
        specification = new NetworkLaneSpecification(networkLaneCriteria);
    }

    @Test
    void givenCriteriaHasOneEach_WhenToPredicate_ExpectPredicateCorrectCount() {
        //GIVEN:
        ServiceType serviceType = new ServiceType();
        serviceType.setId("1");
        serviceType.setName("type 1");

        NetworkLaneLocationFilter originLocations = new NetworkLaneLocationFilter();
        originLocations.setCountryIds(Arrays.asList("10", "20"));
        originLocations.setCountryExtIds(Arrays.asList("10-1", "20-1"));
        originLocations.setStateIds(Arrays.asList("30", "40"));
        originLocations.setStateExtIds(Arrays.asList("30-1", "40-1"));
        originLocations.setCityIds(Arrays.asList("50", "60"));
        originLocations.setCityExtIds(Arrays.asList("50-1", "60-1"));
        originLocations.setFacilityIds(Arrays.asList("70", "80"));
        originLocations.setFacilityExtIds(Arrays.asList("70-1", "80-1"));
        NetworkLaneLocationFilter destinationLocations = new NetworkLaneLocationFilter();
        destinationLocations.setCountryIds(Arrays.asList("110", "120"));
        destinationLocations.setCountryExtIds(Arrays.asList("110-1", "120-1"));
        destinationLocations.setStateIds(Arrays.asList("130", "140"));
        destinationLocations.setStateExtIds(Arrays.asList("130-1", "140-1"));
        destinationLocations.setCityIds(Arrays.asList("150", "160"));
        destinationLocations.setCityExtIds(Arrays.asList("150-1", "160-1"));
        destinationLocations.setFacilityIds(Arrays.asList("170", "180"));
        destinationLocations.setFacilityExtIds(Arrays.asList("170-1", "180-1"));
        NetworkLaneLocationFilter facilityLocations = new NetworkLaneLocationFilter();
        facilityLocations.setCountryIds(Arrays.asList("1110", "1120"));
        facilityLocations.setStateIds(Arrays.asList("1130", "1140"));
        facilityLocations.setCityIds(Arrays.asList("1150", "1160"));
        facilityLocations.setFacilityIds(Arrays.asList("1170", "1180"));


        when(networkLaneCriteria.getOrganizationId()).thenReturn("orgId");
        when(networkLaneCriteria.getServiceTypes()).thenReturn(List.of(serviceType));
        when(networkLaneCriteria.getOriginLocations()).thenReturn(originLocations);
        when(networkLaneCriteria.getDestinationLocations()).thenReturn(destinationLocations);
        when(networkLaneCriteria.getFacilityLocations()).thenReturn(facilityLocations);

        Join<Object, Object> serviceTypeJoin = mockJoin();
        Join<Object, Object> locationHierarchyJoin = mockJoin();
        Join<Object, Object> countryJoin = mockJoin();
        Join<Object, Object> stateJoin = mockJoin();
        Join<Object, Object> cityJoin = mockJoin();
        Join<Object, Object> facilityJoin = mockJoin();
        Join<Object, Object> netowkrLaneSegmentJoin = mockJoin();
        Join<Object, Object> startLocationHierarchyJoin = mockJoin();
        Join<Object, Object> endLocationHierarchyJoin = mockJoin();

        //mocking networkLaneSegment Join to location hierarchy
        when(netowkrLaneSegmentJoin.join(NetworkLaneSegmentEntity_.START_LOCATION_HIERARCHY)).thenReturn(startLocationHierarchyJoin);
        when(netowkrLaneSegmentJoin.join(NetworkLaneSegmentEntity_.END_LOCATION_HIERARCHY)).thenReturn(endLocationHierarchyJoin);

        // mocking join locationHierarchy to locations
        when(locationHierarchyJoin.join(LocationHierarchyEntity_.COUNTRY)).thenReturn(countryJoin);
        when(locationHierarchyJoin.join(LocationHierarchyEntity_.STATE)).thenReturn(stateJoin);
        when(locationHierarchyJoin.join(LocationHierarchyEntity_.CITY)).thenReturn(cityJoin);
        when(locationHierarchyJoin.join(LocationHierarchyEntity_.FACILITY)).thenReturn(facilityJoin);

        //mocking locationHierarchy country state city facility ids predicates
        when(locationHierarchyJoin.get(LocationHierarchyEntity_.COUNTRY_ID)).thenReturn(mockPath());
        when(locationHierarchyJoin.get(LocationHierarchyEntity_.STATE_ID)).thenReturn(mockPath());
        when(locationHierarchyJoin.get(LocationHierarchyEntity_.CITY_ID)).thenReturn(mockPath());
        when(locationHierarchyJoin.get(LocationHierarchyEntity_.FACILITY_ID)).thenReturn(mockPath());

        //mocking networkLane start locationHierarchy country state city facility ids predicates
        Path<Object> facilityPath = mockPath();

        when(startLocationHierarchyJoin.get(LocationHierarchyEntity_.COUNTRY_ID)).thenReturn(mockPath());
        when(startLocationHierarchyJoin.get(LocationHierarchyEntity_.STATE_ID)).thenReturn(mockPath());
        when(startLocationHierarchyJoin.get(LocationHierarchyEntity_.CITY_ID)).thenReturn(mockPath());
        when(startLocationHierarchyJoin.get(LocationHierarchyEntity_.FACILITY_ID)).thenReturn(facilityPath);

        //mocking networkLane end locationHierarchy country state city facility ids predicates
        when(endLocationHierarchyJoin.get(LocationHierarchyEntity_.COUNTRY_ID)).thenReturn(mockPath());
        when(endLocationHierarchyJoin.get(LocationHierarchyEntity_.STATE_ID)).thenReturn(mockPath());
        when(endLocationHierarchyJoin.get(LocationHierarchyEntity_.CITY_ID)).thenReturn(mockPath());
        when(endLocationHierarchyJoin.get(LocationHierarchyEntity_.FACILITY_ID)).thenReturn(facilityPath);

        when(root.join(NetworkLaneEntity_.SERVICE_TYPE)).thenReturn(serviceTypeJoin);
        when(root.join(NetworkLaneEntity_.ORIGIN)).thenReturn(locationHierarchyJoin);
        when(root.join(NetworkLaneEntity_.DESTINATION)).thenReturn(locationHierarchyJoin);
        when(root.join(NetworkLaneEntity_.NETWORK_LANE_SEGMENT_LIST)).thenReturn(netowkrLaneSegmentJoin);

        Predicate predicate = mock(Predicate.class);

        when(serviceTypeJoin.get(BaseEntity_.ID)).thenReturn(mockPath());
        when(serviceTypeJoin.get(ServiceTypeEntity_.NAME)).thenReturn(mockPath());
        when(countryJoin.get(LocationEntity_.EXTERNAL_ID)).thenReturn(mockPath());
        when(stateJoin.get(LocationEntity_.EXTERNAL_ID)).thenReturn(mockPath());
        when(cityJoin.get(LocationEntity_.EXTERNAL_ID)).thenReturn(mockPath());
        when(facilityJoin.get(LocationEntity_.EXTERNAL_ID)).thenReturn(mockPath());
        when(criteriaBuilder.and(any(), any())).thenReturn(predicate);
        //WHEN:
        specification.toPredicate(root, query, criteriaBuilder);
        //THEN:
        Set<String> createdPredicateKeys = specification.getPredicates().keySet();
        assertThat(specification.getPredicates()).isNotNull().hasSize(EXPECTED_PREDICATE_KEYS.size());
        // check expected predicate keys contain all from created
        createdPredicateKeys.forEach(createdPredicateKey ->
                assertThat(EXPECTED_PREDICATE_KEYS).contains(createdPredicateKey)
        );
        // check all from created predicate keys contain expected
        EXPECTED_PREDICATE_KEYS.forEach(expectedPredicateKey ->
                assertThat(createdPredicateKeys).contains(expectedPredicateKey));
        verify(facilityPath, times(2)).in(facilityLocations.getFacilityIds());

    }

    private Join<Object, Object> mockJoin() {
        return mock(Join.class);
    }

    private Path<Object> mockPath() {
        return mock(Path.class);
    }
}
