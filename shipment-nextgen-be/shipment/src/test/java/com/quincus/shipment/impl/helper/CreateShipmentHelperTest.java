package com.quincus.shipment.impl.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShipmentHelperTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private CreateShipmentHelper createShipmentHelper;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void createShipmentEntity_entityHasSubObjects_shouldReturnShipmentEntity() {
        Shipment shipmentDomain = testUtil.createSingleShipmentData();
        OrderShipmentMetadata orderShipmentMetadata = mock(OrderShipmentMetadata.class);

        when(orderShipmentMetadata.organization()).thenReturn(mock(OrganizationEntity.class));
        when(orderShipmentMetadata.customer()).thenReturn(mock(CustomerEntity.class));
        when(orderShipmentMetadata.destination()).thenReturn(mock(AddressEntity.class));
        when(orderShipmentMetadata.origin()).thenReturn(mock(AddressEntity.class));
        when(orderShipmentMetadata.order()).thenReturn(mock(OrderEntity.class));
        when(orderShipmentMetadata.serviceType()).thenReturn(mock(ServiceTypeEntity.class));
        when(orderShipmentMetadata.shipmentJourney()).thenReturn(mock(ShipmentJourneyEntity.class));

        ShipmentEntity shipmentEntity = createShipmentHelper.createShipmentEntity(shipmentDomain, orderShipmentMetadata);

        assertThat(shipmentEntity.getStatus())
                .withFailMessage("Shipment Status is not set to Created.")
                .isEqualTo(ShipmentStatus.CREATED);

        verify(orderShipmentMetadata, times(1)).organization();
        verify(orderShipmentMetadata, times(1)).customer();
        verify(orderShipmentMetadata, times(1)).destination();
        verify(orderShipmentMetadata, times(1)).origin();
        verify(orderShipmentMetadata, times(2)).order();
        verify(orderShipmentMetadata, times(1)).serviceType();
        verify(orderShipmentMetadata, times(1)).shipmentJourney();
    }
}
