package com.quincus.shipment.impl.projection;

import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import com.quincus.shipment.impl.repository.projection.NetworkLaneProjectionListingPage;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.NetworkLaneSegmentService;
import com.quincus.shipment.impl.service.ServiceTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneProjectionListingPageTest {
    @InjectMocks
    private NetworkLaneProjectionListingPage networkLaneProjectionListingPage;
    @Mock
    LocationHierarchyService locationHierarchyService;
    @Mock
    ServiceTypeService serviceTypeService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private NetworkLaneSegmentService networkLaneSegmentService;
    @Mock
    private CriteriaQuery<Tuple> criteriaQuery;
    @Mock
    private Specification<NetworkLaneEntity> specs;
    @Mock
    private Root<NetworkLaneEntity> root;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Predicate predicate;
    @Mock
    private TypedQuery<Tuple> typedQuery;

    @BeforeEach
    void setUp() {
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(criteriaQuery);

        when(criteriaQuery.from(NetworkLaneEntity.class)).thenReturn(root);
        when(specs.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);

        mockPathAlias(root, BaseEntity_.ID);
        mockPathAlias(root, BaseEntity_.CREATE_TIME);
        mockPathAlias(root, NetworkLaneEntity_.SERVICE_TYPE_ID);
        mockPathAlias(root, NetworkLaneEntity_.ORIGIN_ID);
        mockPathAlias(root, NetworkLaneEntity_.DESTINATION_ID);
        mockPathAlias(root, MultiTenantEntity_.ORGANIZATION_ID);
    }

    @Test
    void setupNoTupleResult_findAllWithPagination_verifyTriggeredMethodsAndEmptyEntitiesResult() {
        List<Tuple> tupleResults = new ArrayList<>();
        when(typedQuery.getResultList()).thenReturn(tupleResults);
        List<NetworkLaneEntity> networkLaneEntities = networkLaneProjectionListingPage.findAllWithPagination(specs, mock(Pageable.class));

        assertThat(networkLaneEntities).isEmpty();
        verify(criteriaQuery, times(1)).where(predicate);
        verify(criteriaQuery, times(1)).multiselect(anyList());
    }

    @Test
    void tupleFromNetworkLane_findAllWithPagination_ShouldReturnProperlyMappedResult() {

        List<NetworkLaneSegmentEntity> laneSegmentEntities = new ArrayList<>();
        NetworkLaneSegmentEntity segment1 = new NetworkLaneSegmentEntity();
        segment1.setNetworkLaneId("id-1");
        NetworkLaneSegmentEntity segment2 = new NetworkLaneSegmentEntity();
        segment2.setNetworkLaneId("id-2");
        laneSegmentEntities.add(segment1);
        laneSegmentEntities.add(segment2);
        when(networkLaneSegmentService.findByNetworkLaneIds(List.of("id-1"))).thenReturn(laneSegmentEntities);

        List<ServiceTypeEntity> serviceTypeEntities = new ArrayList<>();
        ServiceTypeEntity serviceType1 = new ServiceTypeEntity();
        serviceType1.setId("service-id-1");
        ServiceTypeEntity serviceType2 = new ServiceTypeEntity();
        serviceType2.setId("service-id-2");
        serviceTypeEntities.add(serviceType1);
        serviceTypeEntities.add(serviceType2);
        when(serviceTypeService.findAllByIds(Set.of("service-id-1"))).thenReturn(serviceTypeEntities);

        List<LocationHierarchyEntity> locationHierarchies = new ArrayList<>();
        LocationHierarchyEntity locationHierarchyEntity1 = new LocationHierarchyEntity();
        locationHierarchyEntity1.setId("origin-id-1");
        LocationHierarchyEntity locationHierarchyEntity2 = new LocationHierarchyEntity();
        locationHierarchyEntity2.setId("destination-id-1");
        locationHierarchies.add(locationHierarchyEntity1);
        locationHierarchies.add(locationHierarchyEntity2);
        when(locationHierarchyService.findAllByIds(Set.of("origin-id-1", "destination-id-1"))).thenReturn(locationHierarchies);

        List<Tuple> tupleResults = new ArrayList<>();
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(BaseEntity_.ID, String.class)).thenReturn("id-1");
        when(tuple.get(NetworkLaneEntity_.SERVICE_TYPE_ID, String.class)).thenReturn("service-id-1");
        when(tuple.get(NetworkLaneEntity_.ORIGIN_ID, String.class)).thenReturn("origin-id-1");
        when(tuple.get(NetworkLaneEntity_.DESTINATION_ID, String.class)).thenReturn("destination-id-1");
        tupleResults.add(tuple);

        when(typedQuery.getResultList()).thenReturn(tupleResults);
        List<NetworkLaneEntity> networkLaneEntities = networkLaneProjectionListingPage.findAllWithPagination(specs, mock(Pageable.class));

        verify(criteriaQuery, times(1)).where(predicate);
        verify(criteriaQuery, times(1)).multiselect(anyList());
        assertThat(networkLaneEntities).isNotEmpty();
        NetworkLaneEntity networkLane = networkLaneEntities.get(0);
        assertThat(networkLane).isNotNull();
        assertThat(networkLane.getNetworkLaneSegmentList()).hasSize(1);
        assertThat(networkLane.getServiceType()).isNotNull();
        assertThat(networkLane.getOrigin()).isNotNull();
        assertThat(networkLane.getDestination()).isNotNull();
    }

    private void mockPathAlias(Root<NetworkLaneEntity> root, String fieldName) {
        Path<Object> path = mock(Path.class);
        when(path.alias(fieldName)).thenReturn(mock(Selection.class));
        when(root.get(fieldName)).thenReturn(path);
    }
}
