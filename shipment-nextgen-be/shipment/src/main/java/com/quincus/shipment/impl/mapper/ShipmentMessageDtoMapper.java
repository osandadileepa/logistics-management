package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ShipmentMessageDtoMapper {
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderIdLabel", target = "orderIdLabel")
    @Mapping(source = "order.trackingUrl", target = "orderTrackingUrl")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "shipmentPackage.id", target = "packageId")
    @Mapping(source = "shipmentPackage.refId", target = "packageRefId")
    @Mapping(source = "order.orderReferences", target = "orderReferences")
    ShipmentMessageDto mapToDto(ShipmentEntity shipmentEntity);

    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "orderIdLabel", target = "order.orderIdLabel")
    @Mapping(source = "orderTrackingUrl", target = "order.trackingUrl")
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "packageId", target = "shipmentPackage.id")
    @Mapping(source = "packageRefId", target = "shipmentPackage.refId")
    @Mapping(source = "orderReferences", target = "order.orderReferences")
    Shipment mapToShipment(ShipmentMessageDto shipmentMessageDto);

    default List<ShipmentMessageDto> mapAllToDto(List<ShipmentEntity> shipmentEntity) {
        return shipmentEntity.stream().map(this::mapToDto).toList();
    }
}
