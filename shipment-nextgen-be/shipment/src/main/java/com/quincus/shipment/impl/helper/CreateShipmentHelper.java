package com.quincus.shipment.impl.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class CreateShipmentHelper {
    public static final String CREATING_SHIPMENT_FROM_ORDER = "Creating Shipment from Order %s";
    public static final String CREATING_REFERENCE_OBJECTS_FROM_ORDER = "Creating Reference Objects from Order %s";
    private final ObjectMapper objectMapper;

    public ShipmentEntity createShipmentEntity(Shipment shipmentDomain, OrderShipmentMetadata orderShipmentMetadata) {
        log.info(String.format(CREATING_SHIPMENT_FROM_ORDER, shipmentDomain.getOrder().getId()));
        shipmentDomain.getOrigin().setExternalId(shipmentDomain.getPickUpLocation());
        shipmentDomain.getDestination().setExternalId(shipmentDomain.getDeliveryLocation());
        shipmentDomain.setStatus(ShipmentStatus.CREATED);
        initializeEtaStatus(shipmentDomain);
        ShipmentUtil.convertOrderTimezonesToUtc(shipmentDomain.getOrder());
        ShipmentEntity shipmentEntity = ShipmentMapper.mapDomainToEntity(shipmentDomain, objectMapper);
        shipmentEntity.setShipmentJourney(orderShipmentMetadata.shipmentJourney());
        return upsertEntities(shipmentEntity, orderShipmentMetadata);
    }

    private ShipmentEntity upsertEntities(ShipmentEntity shipmentEntity, OrderShipmentMetadata orderShipmentMetadata) {
        log.info(String.format(CREATING_REFERENCE_OBJECTS_FROM_ORDER, orderShipmentMetadata.order()));
        shipmentEntity.setOrganization(orderShipmentMetadata.organization());
        shipmentEntity.setOrigin(orderShipmentMetadata.origin());
        shipmentEntity.setDestination(orderShipmentMetadata.destination());
        shipmentEntity.setCustomer(orderShipmentMetadata.customer());
        shipmentEntity.setOrder(orderShipmentMetadata.order());
        shipmentEntity.setServiceType(orderShipmentMetadata.serviceType());
        return shipmentEntity;
    }

    private void initializeEtaStatus(Shipment shipmentDomain) {
        ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);
    }
}