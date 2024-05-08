package com.quincus.shipment.impl.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.ShipmentFetchApi;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.service.ShipmentFetchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShipmentFetchApiImpl implements ShipmentFetchApi {
    private final ShipmentFetchService shipmentFetchService;
    private final ObjectMapper objectMapper;

    @Override
    public List<Shipment> findAllShipmentsByOrderId(final String orderId) {
        return shipmentFetchService.findAllShipmentsByOrderId(orderId)
                .stream()
                .map(shipmentEntity -> ShipmentMapper.mapEntityToDomain(shipmentEntity, objectMapper)).toList();
    }
}
