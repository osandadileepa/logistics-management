package com.quincus.shipment.impl.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionListingPage;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.AlertService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.OrderService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.test_utils.CustomTuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentProjectionListingPageTest {
    @InjectMocks
    private ShipmentProjectionListingPage shipmentProjectionListingPage;
    @Mock
    private OrderService orderService;
    @Mock
    private AddressService addressService;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private AlertService alertService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery criteriaQuery;
    @Mock
    private Root<ShipmentEntity> root;
    @Mock
    private Path<Object> objectPath;
    @Mock
    private Join join;
    @Mock
    private Pageable pageable;
    @Mock
    private TypedQuery<Tuple> tupleTypedQuery;
    @Mock
    private MilestoneService milestoneService;
    @Mock
    private List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;

    @Test
    void testFindAllWithPagination_shouldAcceptEmptyResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(null);
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        assertThat(result).isNotNull();
        assertThat(result).asList().isEmpty();
    }

    private void commonAsserts(List<ShipmentEntity> result) {
        assertThat(result).isNotNull();
        assertThat(result).asList().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("shipmentId");
        assertThat(result.get(0).getShipmentTrackingId()).isEqualTo("shipmentTrackingId");
        assertThat(result.get(0).getOrigin().getId()).isEqualTo("originId");
        assertThat(result.get(0).getDestination().getId()).isEqualTo("destinationId");
        assertThat(result.get(0).getShipmentJourney().getId()).isEqualTo("journeyId");
    }

    @Test
    void testFindAllWithPagination_shouldHaveResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).isNull();
        // Change implementation to not use distinct. Also verify group by is used
        verify(criteriaQuery, times(1)).distinct(false);
        verify(criteriaQuery, times(1)).groupBy(anyList());
    }

    @Test
    void testFindAllWithPagination_shouldHaveResultWhenOrderFound() {
        initializeQuery();
        String orderId = "orderId";
        String orderStatus = "PLANNED";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setStatus(orderStatus);
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        when(orderService.findStatusByShipmentIds(anyList())).thenReturn(List.of(orderEntity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrder()).isNotNull();
        assertThat(result.get(0).getOrder().getId()).isEqualTo(orderId);
        assertThat(result.get(0).getOrder().getStatus()).isEqualTo(orderStatus);
    }

    @Test
    void testFindAllWithPagination_shouldHaveResultWhenAddressFoundButNoLocationHierarchy() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity entity = new AddressEntity();
        entity.setId("entityId");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(entity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).isNull();
    }

    @Test
    void testFindAllWithPagination_shouldHaveResultWhenAddressAndLocationHierarchyFoundButNoSegment() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity entity = new AddressEntity();
        entity.setId("entityId");
        entity.setLocationHierarchy(createLocationHierarchy("lhid"));
        LocationHierarchyEntity lh = new LocationHierarchyEntity();
        lh.setId("lhid");
        entity.setLocationHierarchy(lh);
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(entity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).isNull();
    }

    @Test
    void testFindAllWithPagination_shouldHaveResultWhenAddressAndLocationHierarchyAndSegmentFoundButNoAlert() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity entity = new AddressEntity();
        entity.setId("entityId");
        entity.setLocationHierarchy(createLocationHierarchy("lhid"));
        LocationHierarchyEntity lh = new LocationHierarchyEntity();
        lh.setId("lhid");
        entity.setLocationHierarchy(lh);
        PackageJourneySegmentEntity pjs = new PackageJourneySegmentEntity();
        pjs.setId("segmentId");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(entity));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(pjs));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).isNull();
    }

    @Test
    void testFindAllWithPagination_addressShouldNotHaveLocationHierarchies() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity entity = new AddressEntity();
        entity.setId("entityId");
        entity.setLocationHierarchy(createLocationHierarchy("lhid"));
        LocationHierarchyEntity lh = new LocationHierarchyEntity();
        lh.setId("lhid");
        PackageJourneySegmentEntity pjs = new PackageJourneySegmentEntity();
        pjs.setId("segmentId");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(entity));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(pjs));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNull();
    }

    @Test
    void testFindAllWithPagination_addressShouldHaveValidOrigin() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity entity = new AddressEntity();
        entity.setId("originId");
        entity.setLocationHierarchy(createLocationHierarchy("lhid"));
        ReflectionTestUtils.setField(entity, "locationHierarchyId", "lhid");
        LocationHierarchyEntity lh = new LocationHierarchyEntity();
        lh.setId("lhid");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(entity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNull();
    }

    @Test
    void testFindAllWithPagination_addressShouldHaveValidOriginAndDestination() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
    }

    @Test
    void testFindAllWithPagination_shouldNotMatchSegmentWithJourney() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setShipmentJourneyId("XX");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(segment));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).isNull();
    }

    @Test
    void testFindAllWithPagination_shouldMatchSegmentWithJourney() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("thisSegmentId");
        segment.setShipmentJourneyId("journeyId");
        PackageJourneySegmentEntity segmentRandom = new PackageJourneySegmentEntity();
        segmentRandom.setId("segmentRandom");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(segment, segmentRandom));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).asList().hasSize(1);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getId()).isEqualTo("thisSegmentId");
    }

    @Test
    void testFindAllWithPagination_shouldMatchSegmentsWithJourney() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("thisSegmentId");
        segment.setShipmentJourneyId("journeyId");
        PackageJourneySegmentEntity segment2 = new PackageJourneySegmentEntity();
        segment2.setId("segmentRandom");
        segment2.setShipmentJourneyId("journeyId");
        PackageJourneySegmentEntity segmentRandom = new PackageJourneySegmentEntity();
        segmentRandom.setId("segmentRandom");
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(segment, segmentRandom, segment2));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).asList().hasSize(2);
    }

    @Test
    void testFindAllWithPagination_shouldMatchSegmentsWithJourneyButNoAlerts() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("thisSegmentId");
        segment.setShipmentJourneyId("journeyId");
        ShipmentJourneyEntity journeyRandom = new ShipmentJourneyEntity();
        journeyRandom.setId("journeyRandom");
        PackageJourneySegmentEntity segmentRandom = new PackageJourneySegmentEntity();
        segmentRandom.setId("segmentRandom");
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setId("alertentityId");
        alertEntity.setShipmentJourneyId(journeyRandom.getId());
        alertEntity.setPackageJourneySegmentId(segmentRandom.getId());
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(segment, segmentRandom));
        when(alertService.findByJourneyIdsAndSegmentIds(anyList(), anyList())).thenReturn(List.of(alertEntity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).asList().hasSize(1);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getAlerts()).isNull();
    }

    @Test
    void testFindAllWithPagination_happyPath() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        AddressEntity origin = new AddressEntity();
        origin.setId("originId");
        origin.setLocationHierarchy(createLocationHierarchy("lhidorigin"));
        ReflectionTestUtils.setField(origin, "locationHierarchyId", "lhidorigin");
        LocationHierarchyEntity lhidorigin = new LocationHierarchyEntity();
        lhidorigin.setId("lhidorigin");
        AddressEntity destination = new AddressEntity();
        destination.setId("destinationId");
        destination.setLocationHierarchy(createLocationHierarchy("lhDestination"));
        ReflectionTestUtils.setField(destination, "locationHierarchyId", "lhDestination");
        LocationHierarchyEntity lhDestination = new LocationHierarchyEntity();
        lhDestination.setId("lhDestination");
        PackageJourneySegmentEntity segment = new PackageJourneySegmentEntity();
        segment.setId("thisSegmentId");
        segment.setShipmentJourneyId("journeyId");
        ShipmentJourneyEntity journeyRandom = new ShipmentJourneyEntity();
        journeyRandom.setId("journeyRandom");
        PackageJourneySegmentEntity segmentRandom = new PackageJourneySegmentEntity();
        segmentRandom.setId("segmentRandom");
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setId("alertentityId");
        alertEntity.setShipmentJourneyId(journeyRandom.getId());
        alertEntity.setPackageJourneySegmentId(segment.getId());
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setShipmentId("shipmentId");
        milestone.setMilestoneName("valid");
        MilestoneEntity milestone2 = new MilestoneEntity();
        milestone2.setShipmentId("shipmentIds");
        milestone2.setMilestoneName("notvalid");
        List<MilestoneEntity> milestoneEntitylist = new ArrayList<>();
        milestoneEntitylist.add(milestone);
        milestoneEntitylist.add(milestone2);
        when(addressService.getAddressByIds(anyList())).thenReturn(List.of(origin, destination));
        when(milestoneService.findRecentMilestoneByShipmentIds(anyList())).thenReturn(milestoneEntitylist);
        when(packageJourneySegmentService.findByShipmentJourneyIds(anyList())).thenReturn(List.of(segment, segmentRandom));
        when(alertService.findByJourneyIdsAndSegmentIds(anyList(), anyList())).thenReturn(List.of(alertEntity));
        List<ShipmentEntity> result = shipmentProjectionListingPage.findAllWithPagination(createSpecification(), pageable);
        commonAsserts(result);
        assertThat(result.get(0).getOrigin().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getDestination().getLocationHierarchy()).isNotNull();
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments()).asList().hasSize(1);
        assertThat(result.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getAlerts()).asList().hasSize(1);
        assertThat(result.get(0).getMilestoneEvents().stream().findFirst().get().getMilestoneName()).isEqualTo("valid");
    }

    List<Tuple> createTupleList() {
        CustomTuple customTuple = new CustomTuple();
        customTuple.put(BaseEntity_.ID, "shipmentId");
        customTuple.put(ShipmentEntity_.SHIPMENT_TRACKING_ID, "shipmentTrackingId");
        customTuple.put(ShipmentEntity_.ORDER_ID, "orderId");
        customTuple.put(ShipmentEntity_.CREATE_TIME, Instant.now());
        customTuple.put(ShipmentEntity_.ORIGIN_ID, "originId");
        customTuple.put(ShipmentEntity_.DESTINATION_ID, "destinationId");
        customTuple.put(ShipmentEntity_.SHIPMENT_JOURNEY_ID, "journeyId");
        return List.of(customTuple);
    }


    void initializeQuery() {
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(criteriaQuery.multiselect(anyList())).thenReturn(criteriaQuery);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(ShipmentEntity.class)).thenReturn(root);
        recordGetSelection();
        when(pageable.isPaged()).thenReturn(false);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(tupleTypedQuery);
    }

    ShipmentSpecification createSpecification() {
        Organization organization = new Organization();
        organization.setId("test-id");
        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setPage(1);
        shipmentCriteria.setPerPage(10);
        shipmentCriteria.setOrganization(organization);
        return new ShipmentSpecification(shipmentCriteria, objectMapper, shipmentLocationCoveragePredicates);
    }

    void recordGetSelection() {
        when(root.get(BaseEntity_.ID)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.SHIPMENT_TRACKING_ID)).thenReturn(objectPath);
        when(root.get(BaseEntity_.CREATE_TIME)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.ORDER_ID)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.ORIGIN_ID)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.DESTINATION_ID)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.ORGANIZATION_ID)).thenReturn(join);
        when(root.get(ShipmentEntity_.partnerId)).thenReturn(join);
        when(root.get(BaseEntity_.CREATE_TIME)).thenReturn(objectPath);
        when(root.get(ShipmentEntity_.DELETED)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.STATUS)).thenReturn(objectPath);
    }

    LocationHierarchyEntity createLocationHierarchy(String id) {
        LocationHierarchyEntity entity = new LocationHierarchyEntity();
        entity.setId(id);
        return entity;
    }

}
