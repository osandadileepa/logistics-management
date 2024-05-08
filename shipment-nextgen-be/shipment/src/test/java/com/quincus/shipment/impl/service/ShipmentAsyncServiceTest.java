package com.quincus.shipment.impl.service;

import com.amazonaws.services.outposts.model.OrderStatus;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentAsyncServiceTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ShipmentPostProcessService shipmentPostProcessService;

    @InjectMocks
    private ShipmentAsyncService shipmentAsyncService;

    @Test
    void processAndDispatchShipmentsTest() {
        // Given
        Order order = new Order();
        order.setId("testOrderId");
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);

        when(shipmentService.createOrUpdate(anyList(), anyBoolean())).thenReturn(List.of(shipment));

        List<Shipment> shipmentList = Arrays.asList(shipment, shipment);
        boolean areSegmentsUpdated = false;

        // When
        shipmentAsyncService.processAndDispatchShipments(shipmentList, areSegmentsUpdated);

        // Then
        verify(shipmentService, times(1)).createOrUpdate(anyList(), anyBoolean());
        verify(shipmentPostProcessService, times(1)).sendJourneyToDispatch(anyList(), eq(shipmentJourney), eq(SegmentDispatchType.SHIPMENT_CREATED));
        verify(shipmentPostProcessService, times(1)).sendUpdateToQship(any(Shipment.class));
    }

    @Test
    void processAndDispatchShipmentsTest_shipmentsUpdated() {
        // Given
        Order order = new Order();
        order.setId("testOrderId");
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);
        shipment.setUpdated(true);

        when(shipmentService.createOrUpdate(anyList(), anyBoolean())).thenReturn(List.of(shipment));

        List<Shipment> shipmentList = Arrays.asList(shipment, shipment);
        boolean areSegmentsUpdated = false;

        // When
        shipmentAsyncService.processAndDispatchShipments(shipmentList, areSegmentsUpdated);

        // Then
        verify(shipmentService, times(1)).createOrUpdate(anyList(), anyBoolean());
        verify(shipmentPostProcessService, times(1)).sendJourneyToDispatch(anyList(), eq(shipmentJourney), eq(SegmentDispatchType.SHIPMENT_UPDATED));
        verify(shipmentPostProcessService, times(1)).sendUpdateToQship(any(Shipment.class));
    }

    @Test
    void processAndDispatchShipmentsTest_segmentsUpdated() {
        // Given
        Order order = new Order();
        order.setId("testOrderId");
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);
        shipment.setUpdated(true);
        shipment.setSegmentsUpdatedFromSource(true);

        when(shipmentService.createOrUpdate(anyList(), anyBoolean())).thenReturn(List.of(shipment));

        List<Shipment> shipmentList = Arrays.asList(shipment, shipment);
        boolean areSegmentsUpdated = false;

        // When
        shipmentAsyncService.processAndDispatchShipments(shipmentList, areSegmentsUpdated);

        // Then
        verify(shipmentService, times(1)).createOrUpdate(anyList(), anyBoolean());
        verify(shipmentPostProcessService, times(1)).sendJourneyToDispatch(anyList(), eq(shipmentJourney), eq(SegmentDispatchType.JOURNEY_UPDATED));
        verify(shipmentPostProcessService, times(1)).sendUpdateToQship(any(Shipment.class));
    }


    @Test
    void processAndDispatchShipmentsTest_orderCancelled() {
        // Given
        Order order = new Order();
        order.setId("testOrderId");
        order.setStatus(OrderStatus.CANCELLED.name());
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);
        shipment.setUpdated(true);
        shipment.setSegmentsUpdatedFromSource(true);

        when(shipmentService.createOrUpdate(anyList(), anyBoolean())).thenReturn(List.of(shipment));

        List<Shipment> shipmentList = Arrays.asList(shipment, shipment);
        boolean areSegmentsUpdated = false;

        // When
        shipmentAsyncService.processAndDispatchShipments(shipmentList, areSegmentsUpdated);

        // Then
        verify(shipmentService, times(1)).createOrUpdate(anyList(), anyBoolean());
        verify(shipmentPostProcessService, times(0)).sendJourneyToDispatch(anyList(), eq(shipmentJourney), eq(SegmentDispatchType.JOURNEY_UPDATED));
        verify(shipmentPostProcessService, times(1)).sendUpdateToQship(any(Shipment.class));
    }
}