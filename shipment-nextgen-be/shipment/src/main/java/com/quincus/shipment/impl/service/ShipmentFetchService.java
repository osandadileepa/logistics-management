package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShipmentFetchService {
    static final String ERR_SHIPMENT_NOT_FOUND = "Shipment Id %s not found.";
    private static final String WARN_SHIPMENT_NOT_FOUND_BY_TRACKING_ID_AND_ORGANIZATION_ID = "Shipment not found with shipment tracking id: `%s` and organization id : `%s`";
    private static final String ERR_SHIPMENT_WITH_JOURNEY_ID_NOT_FOUND = "Shipment with Journey Id %s not found.";
    private final UserDetailsProvider userDetailsProvider;
    private final ShipmentRepository shipmentRepository;

    public ShipmentEntity findByShipmentTrackingIdOrThrowException(String shipmentTrackingId) {
        return shipmentRepository.findByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, userDetailsProvider.getCurrentOrganizationId())
                .orElseThrow(() -> new ShipmentNotFoundException(String.format(WARN_SHIPMENT_NOT_FOUND_BY_TRACKING_ID_AND_ORGANIZATION_ID, shipmentTrackingId, userDetailsProvider.getCurrentOrganizationId())));
    }

    public ShipmentEntity findByIdWithFetchOrThrowException(String id) {
        return shipmentRepository.findByIdWithFetch(id, userDetailsProvider.getCurrentOrganizationId())
                .orElseThrow(() -> new ShipmentNotFoundException(String.format(ERR_SHIPMENT_NOT_FOUND, id)));
    }

    @Transactional(readOnly = true)
    public List<ShipmentEntity> findShipmentsForShipmentJourneyUpdate(
            final ObjectMapper objectMapper,
            final String orderId,
            final String journeyId) {
        final List<Tuple> partialShipmentTuple = shipmentRepository
                .findShipmentsForShipmentJourneyUpdate(journeyId, orderId, userDetailsProvider.getCurrentOrganizationId());
        return partialShipmentTuple.stream()
                .map(shp -> ShipmentMapper.toShipmentEntityForShipmentJourneyUpdate(objectMapper, shp))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Optional<ShipmentEntity> findShipmentByTrackingId(String shipmentTrackingId) {
        return shipmentRepository.findByShipmentTrackingIdAndOrgId(shipmentTrackingId, userDetailsProvider.getCurrentOrganizationId());
    }

    public List<ShipmentEntity> findAllByOrderIdUsingTuple(String orderId) {
        List<Tuple> partialShipmentTuple = shipmentRepository.findShipmentsPartialFieldByOrderId(orderId, userDetailsProvider.getCurrentOrganizationId());
        if (CollectionUtils.isEmpty(partialShipmentTuple)) {
            return Collections.emptyList();
        }
        return partialShipmentTuple.stream()
                .map(ShipmentMapper::toShipmentEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ShipmentEntity findByIdOrThrowException(String id) {
        return shipmentRepository.findById(id, userDetailsProvider.getCurrentOrganizationId())
                .orElseThrow(() -> new ShipmentNotFoundException(String.format(ERR_SHIPMENT_NOT_FOUND, id)));
    }

    public List<ShipmentEntity> findByJourneyIdOrThrowException(String journeyId) {
        List<ShipmentEntity> shipmentEntities = shipmentRepository.findByJourneyId(journeyId);
        if (CollectionUtils.isEmpty(shipmentEntities)) {
            throw new ShipmentNotFoundException(String.format(ERR_SHIPMENT_WITH_JOURNEY_ID_NOT_FOUND, journeyId));
        }
        return shipmentEntities;
    }

    public List<ShipmentEntity> findAllShipmentsByOrderId(final String orderId) {
        return shipmentRepository.findAllShipmentsByOrderIdAndOrganizationId(orderId, userDetailsProvider.getCurrentOrganizationId());
    }
}
