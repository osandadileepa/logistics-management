package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderShipmentMetadataPostProcessServiceTest {

    @Mock
    private ArchivingService archivingService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderShipmentMetadataPostProcessService service;

    @Test
    void archiveOrderData_newEntity() throws JsonProcessingException {
        Order order = new Order();
        order.setId("sampleId");
        order.setOrganizationId("sampleOrgId");

        when(archivingService.findByReferenceId(order.getId(), order.getOrganizationId())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(order)).thenReturn("{}");

        service.archiveOrderData(order);

        verify(archivingService, times(1)).save(any(ArchivedEntity.class));
        verify(objectMapper, times(1)).writeValueAsString(order);
    }

    @Test
    void archiveOrderData_existingEntity() throws JsonProcessingException {
        Order order = new Order();
        order.setId("sampleId");
        order.setOrganizationId("sampleOrgId");
        ArchivedEntity archivedEntity = new ArchivedEntity();

        when(archivingService.findByReferenceId(order.getId(), order.getOrganizationId())).thenReturn(Optional.of(archivedEntity));
        when(objectMapper.writeValueAsString(order)).thenReturn("{}");

        service.archiveOrderData(order);

        verify(archivingService, times(1)).save(archivedEntity);
        verify(objectMapper, times(1)).writeValueAsString(order);
    }
}
