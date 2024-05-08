package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.JobType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.CostSegment;
import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CostShipmentMapperTest {

    private final CostShipmentMapper mapper = Mappers.getMapper(CostShipmentMapper.class);

    @Test
    void shouldMapEntityToDomain() {
        ShipmentEntity entity = createShipmentEntity();

        CostShipment result = mapper.mapEntityToDomain(entity);

        assertThat(result.getOrderId()).isEqualTo(entity.getOrder().getId());
        assertThat(result.getOrderIdLabel()).isEqualTo(entity.getOrder().getOrderIdLabel());
        assertThat(result.getShipmentTrackingId()).isEqualTo(entity.getShipmentTrackingId());
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getOrigin()).isEqualTo(entity.getOrigin().getLocationHierarchy().getCity().getName());
        assertThat(result.getDestination()).isEqualTo(entity.getDestination().getLocationHierarchy().getCity().getName());
    }

    private ShipmentEntity createShipmentEntity() {
        ShipmentEntity entity = new ShipmentEntity();
        entity.setShipmentTrackingId(UUID.randomUUID().toString());
        entity.setId(UUID.randomUUID().toString());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(UUID.randomUUID().toString());
        entity.setOrder(orderEntity);
        AddressEntity origin = new AddressEntity();
        LocationHierarchyEntity originLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity originLocation = new LocationEntity();
        originLocation.setName("Toronto City");
        originLocationHierarchy.setCity(originLocation);
        origin.setLocationHierarchy(originLocationHierarchy);
        entity.setOrigin(origin);

        AddressEntity destination = new AddressEntity();
        LocationHierarchyEntity destinationLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity destinationLocation = new LocationEntity();
        destinationLocation.setName("Manila City");
        destinationLocationHierarchy.setCity(destinationLocation);
        destination.setLocationHierarchy(destinationLocationHierarchy);
        entity.setDestination(destination);

        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.setId("1");

        PackageJourneySegmentEntity packageJourneySegment = new PackageJourneySegmentEntity();
        packageJourneySegment.setTransportType(TransportType.AIR);
        packageJourneySegment.setSequence("1");
        packageJourneySegment.setSequence("1");

        shipmentJourneyEntity.addPackageJourneySegment(packageJourneySegment);
        entity.setShipmentJourney(shipmentJourneyEntity);
        return entity;
    }

    @Test
    void shouldMapPackageJourneySegmentToCostSegment() {
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.AIR);
        packageJourneySegment.setSegmentId("1");
        packageJourneySegment.setSequence("1");

        CostSegment result = mapper.mapPackageJourneySegmentToCostSegment(packageJourneySegment);

        assertThat(result.getTransportType()).isEqualTo(packageJourneySegment.getTransportType());
        assertThat(result.getSegmentId()).isEqualTo(packageJourneySegment.getSegmentId());
        assertThat(result.getSequenceNo()).isEqualTo(packageJourneySegment.getSequence());
    }

    @Test
    void shouldMapPackageJourneySegmentsToCostSegments() {
        List<JobType> defaultJobTypes = List.of(JobType.PICK_UP, JobType.DROP_OFF);
        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.AIR);
        packageJourneySegment.setSegmentId("1");
        packageJourneySegment.setSequence("1");
        List<PackageJourneySegment> packageJourneySegments = List.of(packageJourneySegment);

        List<CostSegment> result = mapper.mapPackageJourneySegmentsToCostSegments(packageJourneySegments);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransportType()).isEqualTo(packageJourneySegment.getTransportType());
        assertThat(result.get(0).getSegmentId()).isEqualTo(packageJourneySegment.getSegmentId());
        assertThat(result.get(0).getSequenceNo()).isEqualTo(packageJourneySegment.getSequence());
        result.get(0).getJobTypes().forEach(jobType -> assertThat(defaultJobTypes).contains(jobType));
    }

    @Test
    void shouldMapEntitiesForCostListing() {
        ShipmentEntity entity = createShipmentEntity();

        List<CostShipment> result = mapper.mapEntitiesForCostListing(List.of(entity));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(entity.getOrder().getId());
        assertThat(result.get(0).getOrderIdLabel()).isEqualTo(entity.getOrder().getOrderIdLabel());
        assertThat(result.get(0).getShipmentTrackingId()).isEqualTo(entity.getShipmentTrackingId());
        assertThat(result.get(0).getId()).isEqualTo(entity.getId());
        assertThat(result.get(0).getOrigin()).isEqualTo(entity.getOrigin().getLocationHierarchy().getCity().getName());
        assertThat(result.get(0).getDestination()).isEqualTo(entity.getDestination().getLocationHierarchy().getCity().getName());

        assertThat(result.get(0).getSegments().get(0).getTransportType()).isEqualTo(entity.getShipmentJourney().getPackageJourneySegments().get(0).getTransportType());
        assertThat(result.get(0).getSegments().get(0).getSegmentId()).isEqualTo(entity.getShipmentJourney().getPackageJourneySegments().get(0).getId());
        assertThat(result.get(0).getSegments().get(0).getSequenceNo()).isEqualTo(entity.getShipmentJourney().getPackageJourneySegments().get(0).getSequence());
    }
}
