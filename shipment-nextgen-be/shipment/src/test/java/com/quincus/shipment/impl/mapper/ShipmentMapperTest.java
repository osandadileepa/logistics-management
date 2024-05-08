package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final MapperTestUtil mapperTestUtil = MapperTestUtil.getInstance();

    private final ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapDomainToEntity_shipmentDomain_shouldReturnShipmentEntity() {
        final Shipment domain = testUtil.createSingleShipmentData();

        final ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);

        mapperTestUtil.shipmentDomainToEntity_commonAsserts(domain, entity);
        assertThat(entity.getNotes()).isEqualTo(domain.getNotes());
    }

    @Test
    void mapDomainToEntity_shipmentDomainNull_shouldReturnNull() {
        assertThat(ShipmentMapper.mapDomainToEntity(null, objectMapper)).isNull();
    }

    @Test
    void mapDomainToEntity_mapperNull_shouldReturnNull() {
        final Shipment domain = testUtil.createSingleShipmentData();
        assertThat(ShipmentMapper.mapDomainToEntity(domain, null)).isNull();
    }

    @Test
    void mapEntityToDomain_shipmentEntity_shouldReturnShipmentDomain() {
        final ShipmentEntity entity = mapperTestUtil.createSampleShipmentEntity();

        final Shipment domain = ShipmentMapper.mapEntityToDomain(entity, objectMapper);
        assertThat(domain).isNotNull();
        mapperTestUtil.shipmentEntityToDomain_commonAsserts(entity, domain);
    }

    @Test
    void mapEntityToDomain_shipmentEntityNull_shouldReturnNull() {
        assertThat(ShipmentMapper.mapEntityToDomain(null, objectMapper)).isNull();
    }

    @Test
    void mapEntityToDomain_mapperNull_shouldReturnNull() {
        final ShipmentEntity entity = mapperTestUtil.createSampleShipmentEntity();
        assertThat(ShipmentMapper.mapEntityToDomain(entity, null)).isNull();
    }

    @Test
    void mapDomainListToEntityListShipment_shipmentDomainList_shouldReturnShipmentEntityList() {
        final List<Shipment> domainList = List.of(testUtil.createSingleShipmentData());

        final List<ShipmentEntity> entityList = ShipmentMapper.mapDomainListToEntityListShipment(domainList, objectMapper);
        for (int i = 0; i < entityList.size(); i++) {
            ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domainList.get(i).getShipmentJourney());
            entityList.get(i).setShipmentJourney(shipmentJourneyEntity);
        }

        assertThat(entityList).hasSameSizeAs(domainList);
        for (int i = 0; i < domainList.size(); i++) {
            mapperTestUtil.shipmentDomainToEntity_commonAsserts(domainList.get(i), entityList.get(i));
        }
    }

    @Test
    void mapDomainListToEntityListShipment_shipmentDomainListNull_shouldReturnNull() {
        final List<ShipmentEntity> entityList = ShipmentMapper.mapDomainListToEntityListShipment(null, objectMapper);
        assertThat(entityList).isEmpty();
    }

    @Test
    void mapDomainListToEntityListShipment_mapperNull_shouldReturnNull() {
        final List<Shipment> domainList = List.of(testUtil.createSingleShipmentData());

        final List<ShipmentEntity> entityList = ShipmentMapper.mapDomainListToEntityListShipment(domainList, null);

        assertThat(entityList).isEmpty();
    }

    @Test
    void mapEntityListToDomainListShipment_shipmentEntityList_shouldReturnShipmentDomainList() {
        final List<ShipmentEntity> entityList = List.of(mapperTestUtil.createSampleShipmentEntity());

        final List<Shipment> domainList = ShipmentMapper.mapEntityListToDomainListShipment(entityList, objectMapper);

        assertThat(entityList).hasSameSizeAs(domainList);
        for (int i = 0; i < entityList.size(); i++) {
            mapperTestUtil.shipmentEntityToDomain_commonAsserts(entityList.get(i), domainList.get(i));
        }
    }

    @Test
    void mapEntityListToDomainListShipment_shipmentEntityListNull_shouldReturnNull() {
        final List<Shipment> domainList = ShipmentMapper.mapEntityListToDomainListShipment(null, objectMapper);

        assertThat(domainList).isEmpty();
    }

    @Test
    void mapEntityListToDomainListShipment_mapperNull_shouldReturnNull() {
        final List<ShipmentEntity> entityList = List.of(mapperTestUtil.createSampleShipmentEntity());

        final List<Shipment> domainList = ShipmentMapper.mapEntityListToDomainListShipment(entityList, null);

        assertThat(domainList).isEmpty();
    }

    @Test
    void testMapEntitiesForListing_shouldMapAndReplaceOldSegments() {
        String orderId = "order1";
        String shipmentJourneyId = "oldShipmentJourney-id";
        List<ShipmentEntity> shipmentEntities = setupShipmentEntities(orderId, shipmentJourneyId);

        List<PackageJourneySegmentEntity> newSegments = new ArrayList<>();
        PackageJourneySegmentEntity newSegment = new PackageJourneySegmentEntity();
        newSegment.setRefId("0");
        newSegment.setSequence("0");
        newSegment.setTransportType(TransportType.GROUND);
        newSegment.setType(SegmentType.FIRST_MILE);
        newSegment.setId("new-segment-id");
        newSegment.setShipmentJourneyId(shipmentJourneyId);
        PackageJourneySegmentEntity newSegment2 = new PackageJourneySegmentEntity();
        newSegment2.setRefId("1");
        newSegment2.setSequence("0");
        newSegment2.setTransportType(TransportType.GROUND);
        newSegment2.setType(SegmentType.MIDDLE_MILE);
        newSegment2.setId("new-segment-id-2");
        newSegment2.setShipmentJourneyId(shipmentJourneyId);
        newSegments.add(newSegment);
        newSegments.add(newSegment2);
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId(shipmentJourneyId);
        journeyEntity.addAllPackageJourneySegments(newSegments);
        shipmentEntities.get(0).setShipmentJourney(journeyEntity);
        List<Shipment> shipments = ShipmentMapper.mapEntitiesForListing(shipmentEntities);
        assertThat(shipments.get(0).getOrder().getId()).isEqualTo(orderId);
        assertThat(shipments.get(0).getOrder().getStatus()).isNotNull();
        assertThat(shipments.get(0).getShipmentJourney().getPackageJourneySegments()).hasSize(2);
        assertThat(shipments.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getSegmentId()).contains("new");
        assertThat(shipments.get(0).getShipmentJourney().getPackageJourneySegments().get(1).getSegmentId()).contains("new");
    }

    @Test
    void testMapEntitiesForListing_shouldIgnoreMapWhenNoMatch() {
        String orderId = "order1";
        String shipmentJourneyId = "oldShipmentJourney-id";
        List<ShipmentEntity> shipmentEntities = setupShipmentEntities(orderId, "shipmentJourneyId");

        List<PackageJourneySegmentEntity> newSegments = new ArrayList<>();
        PackageJourneySegmentEntity newSegment = new PackageJourneySegmentEntity();
        newSegment.setRefId("0");
        newSegment.setTransportType(TransportType.GROUND);
        newSegment.setId("new-segment-id");
        newSegment.setShipmentJourneyId(shipmentJourneyId);
        newSegment.setType(SegmentType.FIRST_MILE);
        PackageJourneySegmentEntity newSegment2 = new PackageJourneySegmentEntity();
        newSegment2.setRefId("1");
        newSegment2.setId("new-segment-id-2");
        newSegment2.setType(SegmentType.MIDDLE_MILE);
        newSegment2.setTransportType(TransportType.GROUND);
        newSegment2.setShipmentJourneyId(shipmentJourneyId);
        newSegments.add(newSegment);
        newSegments.add(newSegment2);

        List<Shipment> shipments = ShipmentMapper.mapEntitiesForListing(shipmentEntities);
        assertThat(shipments.get(0).getOrder().getId()).isEqualTo(orderId);
        assertThat(shipments.get(0).getOrder().getStatus()).isNotNull();
        assertThat(shipments.get(0).getShipmentJourney().getPackageJourneySegments()).hasSize(1);
        assertThat(shipments.get(0).getShipmentJourney().getPackageJourneySegments().get(0).getSegmentId()).contains("old");
    }

    private List<ShipmentEntity> setupShipmentEntities(String orderId, String shipmentJourneyId) {
        String shipmentId = "1-shipment-entity";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setStatus(Root.STATUS_CREATED);
        shipmentEntity.setOrder(orderEntity);
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId(shipmentJourneyId);
        List<PackageJourneySegmentEntity> segmentsOld = new ArrayList<>();
        PackageJourneySegmentEntity oldSegment = new PackageJourneySegmentEntity();
        oldSegment.setRefId("0");
        oldSegment.setId("old-segment-id");
        oldSegment.setShipmentJourneyId(shipmentJourneyId);
        oldSegment.setType(SegmentType.FIRST_MILE);
        oldSegment.setTransportType(TransportType.GROUND);
        segmentsOld.add(oldSegment);
        shipmentJourneyEntity.addAllPackageJourneySegments(segmentsOld);
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        List<ShipmentEntity> shipmentEntities = new ArrayList<>();
        shipmentEntities.add(shipmentEntity);
        return shipmentEntities;
    }

    @Test
    void giveTupleForShipmentJourneyUpdate_whenMapping_thenCorrectValueIsSet() {
        final Tuple tupleForShipmentJourneyUpdateMock = Mockito.mock(Tuple.class);
        final String givenShipmentId = UUID.randomUUID().toString();
        final String givenPartnerId = UUID.randomUUID().toString();
        final boolean isDeleted = false;

        when(tupleForShipmentJourneyUpdateMock.get(anyString(), any())).thenReturn(null);
        when(tupleForShipmentJourneyUpdateMock.get(BaseEntity_.ID, String.class)).thenReturn(givenShipmentId);
        when(tupleForShipmentJourneyUpdateMock.get(ShipmentEntity_.PARTNER_ID, String.class)).thenReturn(givenPartnerId);
        when(tupleForShipmentJourneyUpdateMock.get(ShipmentEntity_.ORDER, OrderEntity.class)).thenReturn(null);
        when(tupleForShipmentJourneyUpdateMock.get(ShipmentEntity_.SHIPMENT_JOURNEY, ShipmentJourneyEntity.class)).thenReturn(null);
        when(tupleForShipmentJourneyUpdateMock.get(ShipmentEntity_.DELETED, Boolean.class)).thenReturn(isDeleted);

        final ShipmentEntity actualResult = ShipmentMapper
                .toShipmentEntityForShipmentJourneyUpdate(objectMapper, tupleForShipmentJourneyUpdateMock);

        assertThat(actualResult.getId()).isEqualTo(givenShipmentId);
        assertThat(actualResult.getPartnerId()).isEqualTo(givenPartnerId);
        assertThat(actualResult.isDeleted()).isFalse();
    }

    @Test
    void toShipmentForShipmentJourneyUpdate_shipmentEntity_shouldOnlyMapSelectedFields() {
        ShipmentEntity shipmentEntity = mapperTestUtil.createSampleShipmentEntity();
        Shipment shipment = ShipmentMapper.toShipmentForShipmentJourneyUpdate(shipmentEntity);

        assertThat(shipment).isNotNull();
        assertThat(shipment.getId()).isEqualTo(shipmentEntity.getId());
        assertThat(shipment.getUserId()).isEqualTo(shipmentEntity.getUserId());
        assertThat(shipment.getShipmentTrackingId()).isEqualTo(shipmentEntity.getShipmentTrackingId());
        assertThat(shipment.getPartnerId()).isEqualTo(shipmentEntity.getPartnerId());
        assertThat(shipment.getOrder()).isNotNull();
        assertThat(shipment.getOrganization()).isNotNull();
        assertThat(shipment.getShipmentJourney()).isNull();
        assertThat(shipment.getShipmentPackage()).isNotNull();
        assertThat(shipment.getShipmentReferenceId()).isEqualTo(shipmentEntity.getShipmentReferenceId());
        assertThat(shipment.getShipmentTags()).isEqualTo(shipmentEntity.getShipmentTags());
        assertThat(shipment.getOrigin()).isNotNull();
        assertThat(shipment.getDestination()).isNotNull();
        assertThat(shipment.getSender()).isNotNull();
        assertThat(shipment.getConsignee()).isNotNull();
        assertThat(shipment.getStatus()).isEqualTo(shipmentEntity.getStatus());
        assertThat(shipment.getServiceType()).isNotNull();
        assertThat(shipment.getNotes()).isEqualTo(shipmentEntity.getNotes());
        assertThat(shipment.getExternalOrderId()).isEqualTo(shipmentEntity.getExternalOrderId());
        assertThat(shipment.getInternalOrderId()).isEqualTo(shipmentEntity.getInternalOrderId());
        assertThat(shipment.getCustomerOrderId()).isEqualTo(shipmentEntity.getCustomerOrderId());
    }
}
