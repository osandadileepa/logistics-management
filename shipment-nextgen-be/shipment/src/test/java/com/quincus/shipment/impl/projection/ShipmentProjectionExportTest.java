package com.quincus.shipment.impl.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.AddressEntity_;
import com.quincus.shipment.impl.repository.entity.CustomerEntity_;
import com.quincus.shipment.impl.repository.entity.LocationEntity_;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionExport;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.test_utils.CustomTuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentProjectionExportTest {
    @InjectMocks
    private ShipmentProjectionExport shipmentProjectionExport;
    @Mock
    private MilestoneService milestoneService;
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
    private TupleElement tuple;
    @Mock
    private TypedQuery<Tuple> tupleTypedQuery;
    @Mock
    private Join join;
    @Mock
    private ListJoin listJoin;
    @Mock
    private List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    @Mock
    private MeasurementUnit measurementUnit;

    @Test
    void testFindAll_shouldAcceptEmptyResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(null);
        List<ShipmentEntity> result = shipmentProjectionExport.findAll(createSpecification());
        assertThat(result).asList().isEmpty();
    }

    @Test
    void testFindAll_shouldHaveResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        List<ShipmentEntity> result = shipmentProjectionExport.findAll(createSpecification());
        commonAsserts(result);
    }

    @Test
    void testFindAll_shouldHaveResulWithoutMilestone() {
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setShipmentId("notFound");
        List<MilestoneEntity> milestoneEntitylist = new ArrayList<>();
        milestoneEntitylist.add(milestone);
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        when(milestoneService.findRecentMilestoneByShipmentIds(anyList())).thenReturn(milestoneEntitylist);
        List<ShipmentEntity> result = shipmentProjectionExport.findAll(createSpecification());
        commonAsserts(result);
        assertThat(result.get(0).getMilestoneEvents()).isNotNull();
    }

    @Test
    void testFindAll_shouldHaveResulWithOneMilestone() {
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setCreateTime(Instant.now());
        milestone.setShipmentId("notFound");
        milestone.setMilestoneName("DELAYED");
        MilestoneEntity milestone2 = new MilestoneEntity();
        milestone2.setShipmentId("shipmentId");
        milestone2.setMilestoneName("BOOKED");
        milestone2.setCreateTime(Instant.now().minusMillis(1000));

        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        when(milestoneService.findRecentMilestoneByShipmentIds(anyList())).thenReturn(List.of(milestone, milestone2));
        List<ShipmentEntity> result = shipmentProjectionExport.findAll(createSpecification());
        commonAsserts(result);
        assertThat(new ArrayList<>(result.get(0).getMilestoneEvents()))
                .asList()
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(milestone2);

    }

    @Test
    void testFindAll_shouldHaveResulWithTwoMilestones() {
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setCreateTime(Instant.now());
        milestone.setShipmentId("notFound");
        milestone.setMilestoneName("DELAYED");
        MilestoneEntity milestone2 = new MilestoneEntity();
        milestone2.setShipmentId("shipmentId");
        milestone2.setMilestoneName("BOOKED");
        milestone2.setCreateTime(Instant.now().minusMillis(1000));
        MilestoneEntity milestone3 = new MilestoneEntity();
        milestone3.setShipmentId("shipmentIds");
        milestone3.setMilestoneName("ONTIME");
        milestone3.setCreateTime(Instant.now().minusMillis(10000));
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        when(milestoneService.findRecentMilestoneByShipmentIds(anyList())).thenReturn(List.of(milestone, milestone2, milestone3));
        List<ShipmentEntity> result = shipmentProjectionExport.findAll(createSpecification());
        commonAsserts(result);
        assertThat(new ArrayList<>(result.get(0).getMilestoneEvents()))
                .asList()
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(milestone2);

    }

    List<Tuple> createTupleList() {
        CustomTuple customTuple = new CustomTuple();
        customTuple.put(BaseEntity_.ID, "shipmentId");
        customTuple.put(BaseEntity_.CREATE_TIME, Instant.now());
        customTuple.put(ShipmentEntity_.SHIPMENT_TRACKING_ID, "shipmentTrackingId");
        customTuple.put(ShipmentEntity_.SERVICE_TYPE, "serviceType");
        customTuple.put(ShipmentEntity_.CUSTOMER, "customerName");
        customTuple.put(PackageDimensionEntity_.MEASUREMENT_UNIT, MeasurementUnit.METRIC);
        customTuple.put(PackageDimensionEntity_.VOLUME_WEIGHT, BigDecimal.valueOf(10));
        customTuple.put(PackageDimensionEntity_.GROSS_WEIGHT, BigDecimal.valueOf(120));
        customTuple.put(ShipmentEntity_.ETA_STATUS, EtaStatus.BOOKED);
        return List.of(customTuple);
    }

    private void commonAsserts(List<ShipmentEntity> result) {
        assertThat(result).isNotNull();
        assertThat(result).asList().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("shipmentId");
        assertThat(result.get(0).getShipmentTrackingId()).isEqualTo("shipmentTrackingId");
        assertThat(result.get(0).getServiceType().getName()).isEqualTo("serviceType");
        assertThat(result.get(0).getCustomer().getName()).isEqualTo("customerName");
        assertThat(result.get(0).getEtaStatus()).isEqualTo(EtaStatus.BOOKED);
    }

    void initializeQuery() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(ShipmentEntity.class)).thenReturn(root);

        lenient().when(root.join(ShipmentEntity_.customer, JoinType.LEFT)).thenReturn(join);
        lenient().when(root.join(ShipmentEntity_.serviceType, JoinType.LEFT)).thenReturn(join);
        lenient().when(root.get(ShipmentEntity_.partnerId)).thenReturn(join);

        lenient().when(join.join(LocationHierarchyEntity_.country, JoinType.LEFT)).thenReturn(join);
        lenient().when(join.join(AddressEntity_.locationHierarchy, JoinType.LEFT)).thenReturn(join);
        lenient().when(join.join(ShipmentJourneyEntity_.packageJourneySegments, JoinType.LEFT)).thenReturn(listJoin);
        lenient().when(join.join(PackageJourneySegmentEntity_.startLocationHierarchy, JoinType.LEFT)).thenReturn(listJoin);
        lenient().when(listJoin.join(LocationHierarchyEntity_.country, JoinType.LEFT)).thenReturn(listJoin);

        lenient().when(join.get(ServiceTypeEntity_.name)).thenReturn(objectPath);
        lenient().when(join.get(CustomerEntity_.name)).thenReturn(objectPath);
        lenient().when(join.get(LocationEntity_.name)).thenReturn(objectPath);
        lenient().when(listJoin.get(PackageJourneySegmentEntity_.refId)).thenReturn(objectPath);

        recordGetSelection();
        when(entityManager.createQuery(criteriaQuery)).thenReturn(tupleTypedQuery);
    }

    ShipmentSpecification createSpecification() {
        Organization organization = new Organization();
        organization.setId("test-id");
        ShipmentCriteria shipmentCriteria = new ShipmentCriteria();
        shipmentCriteria.setOrganization(organization);
        return new ShipmentSpecification(shipmentCriteria, objectMapper, shipmentLocationCoveragePredicates);
    }

    void recordGetSelection() {
        lenient().when(root.get(BaseEntity_.ID)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.SHIPMENT_TRACKING_ID)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.ETA_STATUS)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.ORGANIZATION_ID)).thenReturn(join);
        lenient().when(root.get(ShipmentEntity_.INSTRUCTIONS)).thenReturn(objectPath);
        lenient().when(root.get(BaseEntity_.CREATE_TIME)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.DELETED)).thenReturn(objectPath);
        lenient().when(root.get(ShipmentEntity_.STATUS)).thenReturn(objectPath);
    }

    @Test
    void whenFindAllShipmentTrackingIdsShouldReturnResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(createTupleList());
        List<Map<String, String>> result = shipmentProjectionExport.findAllShipmentTrackingIds(createSpecification(), Pageable.unpaged());
        Map<String, String> expected = new HashMap<>();
        expected.put(ShipmentEntity_.SHIPMENT_TRACKING_ID, "shipmentTrackingId");
        expected.put(PackageDimensionEntity_.MEASUREMENT_UNIT, MeasurementUnit.METRIC.getLabel());
        expected.put(PackageDimensionEntity_.VOLUME_WEIGHT, BigDecimal.valueOf(10).toString());
        expected.put(PackageDimensionEntity_.GROSS_WEIGHT, BigDecimal.valueOf(120).toString());
        assertThat(result).contains(expected);
    }

    @Test
    void whenFindAllShipmentTrackingIdsShouldReturnEmptyResult() {
        initializeQuery();
        when(tupleTypedQuery.getResultList()).thenReturn(List.of());
        List<Map<String, String>> result = shipmentProjectionExport.findAllShipmentTrackingIds(createSpecification(), Pageable.unpaged());
        assertThat(result).isEmpty();
    }
}
