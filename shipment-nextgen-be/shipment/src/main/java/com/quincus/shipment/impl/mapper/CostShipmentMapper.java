package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.JobType;
import com.quincus.shipment.api.domain.CostSegment;
import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = AddressMapper.class, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CostShipmentMapper {

    @Mapping(source = "shipmentEntity.order.id", target = "orderId")
    @Mapping(source = "shipmentEntity.order.orderIdLabel", target = "orderIdLabel")
    @Mapping(target = "origin", expression = "java(AddressMapper.mapEntityToDomain(shipmentEntity.getOrigin()).getCityName())")
    @Mapping(target = "destination", expression = "java(AddressMapper.mapEntityToDomain(shipmentEntity.getDestination()).getCityName())")
    CostShipment mapEntityToDomain(ShipmentEntity shipmentEntity);

    @Mapping(source = "packageJourneySegment.transportType", target = "transportType")
    @Mapping(source = "packageJourneySegment.segmentId", target = "segmentId")
    @Mapping(source = "packageJourneySegment.sequence", target = "sequenceNo")
    @Mapping(source = "packageJourneySegment.refId", target = "refId")
    CostSegment mapPackageJourneySegmentToCostSegment(PackageJourneySegment packageJourneySegment);

    default List<CostSegment> mapPackageJourneySegmentsToCostSegments(List<PackageJourneySegment> packageJourneySegments) {
        List<JobType> defaultJobTypes = Arrays.asList(JobType.values());
        return packageJourneySegments.stream()
                .map(packageJourneySegment -> mapPackageJourneySegmentToCostSegment(packageJourneySegment, defaultJobTypes)).toList();
    }

    default CostSegment mapPackageJourneySegmentToCostSegment(PackageJourneySegment packageJourneySegment, List<JobType> jobTypes) {
        CostSegment costSegment = mapPackageJourneySegmentToCostSegment(packageJourneySegment);
        costSegment.setJobTypes(jobTypes);
        return costSegment;
    }

    default List<CostShipment> mapEntitiesForCostListing(List<ShipmentEntity> entities) {
        return Optional.ofNullable(entities)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::mapEntityWithJourneyToCostShipment)
                .toList();
    }

    private CostShipment mapEntityWithJourneyToCostShipment(ShipmentEntity entity) {
        CostShipment costShipment = mapEntityToDomain(entity);
        ShipmentJourney shipmentJourney = ShipmentJourneyMapper.mapEntityToDomainForListing(entity.getShipmentJourney());

        if (shipmentJourney != null) {
            List<CostSegment> segments = mapPackageJourneySegmentsToCostSegments(shipmentJourney.getPackageJourneySegments());
            costShipment.setSegments(segments);
            costShipment.setTotalSegments(segments.size());
        }
        return costShipment;
    }

}
