package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentMessageDtoMapperTest {
    private final ShipmentMessageDtoMapper shipmentMessageDtoMapper = Mappers.getMapper(ShipmentMessageDtoMapper.class);

    private final MapperTestUtil mapperTestUtil = MapperTestUtil.getInstance();

    @Test
    void mapToDto() {
        ShipmentEntity shipmentEntity = mapperTestUtil.createSampleShipmentEntity();
        ShipmentMessageDto shipmentMessageDto = shipmentMessageDtoMapper.mapToDto(shipmentEntity);

        assertThat(shipmentMessageDto.getId()).isEqualTo(shipmentEntity.getId());
        assertThat(shipmentMessageDto.getOrderId()).isEqualTo(shipmentEntity.getOrder().getId());
        assertThat(shipmentMessageDto.getOrganizationId()).isEqualTo(shipmentEntity.getOrganization().getId());
        assertThat(shipmentMessageDto.getPackageId()).isEqualTo(shipmentEntity.getShipmentPackage().getId());
        assertThat(shipmentMessageDto.getPackageRefId()).isEqualTo(shipmentEntity.getShipmentPackage().getRefId());
        assertThat(shipmentMessageDto.getUserId()).isEqualTo(shipmentEntity.getUserId());
        assertThat(shipmentMessageDto.getPartnerId()).isEqualTo(shipmentEntity.getPartnerId());
        assertThat(shipmentMessageDto.getSender()).isEqualTo(shipmentEntity.getSender());
        assertThat(shipmentMessageDto.getConsignee()).isEqualTo(shipmentEntity.getConsignee());
        assertThat(shipmentMessageDto.getOrderIdLabel()).isEqualTo(shipmentEntity.getOrder().getOrderIdLabel());
        assertThat(shipmentMessageDto.getOrderTrackingUrl()).isEqualTo(shipmentEntity.getOrder().getTrackingUrl());

        // Mapping for the additional fields
        assertThat(shipmentMessageDto.getShipmentReferenceId()).isEqualTo(shipmentEntity.getShipmentReferenceId());
        assertThat(shipmentMessageDto.getExternalOrderId()).isEqualTo(shipmentEntity.getExternalOrderId());
        assertThat(shipmentMessageDto.getInternalOrderId()).isEqualTo(shipmentEntity.getInternalOrderId());
        assertThat(shipmentMessageDto.getCustomerOrderId()).isEqualTo(shipmentEntity.getCustomerOrderId());
        assertThat(shipmentMessageDto.getOrderReferences().get(0).getExternalId())
                .isEqualTo(shipmentEntity.getOrder().getOrderReferences().get(0).getExternalId());
    }

    @Test
    void mapToShipment() {
        ShipmentMessageDto shipmentMessageDto = mapperTestUtil.createDummyShipmentDto();
        Shipment shipment = shipmentMessageDtoMapper.mapToShipment(shipmentMessageDto);

        assertThat(shipmentMessageDto.getId()).isEqualTo(shipment.getId());
        assertThat(shipmentMessageDto.getOrderId()).isEqualTo(shipment.getOrder().getId());
        assertThat(shipmentMessageDto.getOrganizationId()).isEqualTo(shipment.getOrganization().getId());
        assertThat(shipmentMessageDto.getPackageId()).isEqualTo(shipment.getShipmentPackage().getId());
        assertThat(shipmentMessageDto.getPackageRefId()).isEqualTo(shipment.getShipmentPackage().getRefId());
        assertThat(shipmentMessageDto.getUserId()).isEqualTo(shipment.getUserId());
        assertThat(shipmentMessageDto.getPartnerId()).isEqualTo(shipment.getPartnerId());
        assertThat(shipmentMessageDto.getSender()).isEqualTo(shipment.getSender());
        assertThat(shipmentMessageDto.getConsignee()).isEqualTo(shipment.getConsignee());
        assertThat(shipmentMessageDto.getOrderIdLabel()).isEqualTo(shipment.getOrder().getOrderIdLabel());
        assertThat(shipmentMessageDto.getOrderTrackingUrl()).isEqualTo(shipment.getOrder().getTrackingUrl());

        // Mapping for the additional fields
        assertThat(shipmentMessageDto.getShipmentReferenceId()).isEqualTo(shipment.getShipmentReferenceId());
        assertThat(shipmentMessageDto.getExternalOrderId()).isEqualTo(shipment.getExternalOrderId());
        assertThat(shipmentMessageDto.getInternalOrderId()).isEqualTo(shipment.getInternalOrderId());
        assertThat(shipmentMessageDto.getCustomerOrderId()).isEqualTo(shipment.getCustomerOrderId());
        assertThat(shipmentMessageDto.getOrderReferences()).isNotNull().hasSize(2);
        assertThat(shipmentMessageDto.getOrderReferences().get(0).getLabel())
                .isEqualTo(shipment.getOrder().getOrderReferences().get(0).getLabel());
    }

}
