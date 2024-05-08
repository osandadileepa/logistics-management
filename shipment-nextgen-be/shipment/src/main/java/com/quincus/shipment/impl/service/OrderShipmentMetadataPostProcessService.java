package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShipmentMetadataPostProcessService {
    private final ArchivingService archivingService;
    private final ObjectMapper objectMapper;

    @Async("threadPoolTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void archiveOrderData(Order order) {
        Optional<ArchivedEntity> archivedEntityOptional = archivingService.findByReferenceId(order.getId(), order.getOrganizationId());
        ArchivedEntity archivedOrderData = archivedEntityOptional.orElseGet(() -> {
            ArchivedEntity newEntity = new ArchivedEntity();
            newEntity.setReferenceId(order.getId());
            newEntity.setOrganizationId(order.getOrganizationId());
            newEntity.setClassName(order.getClass().getName());
            return newEntity;
        });
        setData(order, archivedOrderData);
        archivingService.save(archivedOrderData);
    }

    private void setData(Order order, ArchivedEntity archivedEntity) {
        try {
            archivedEntity.setData(objectMapper.writeValueAsString(order));
        } catch (JsonProcessingException e) {
            log.error("Error parsing order to archive entity with order id `{}`", order.getId(), e);
        }
    }
}