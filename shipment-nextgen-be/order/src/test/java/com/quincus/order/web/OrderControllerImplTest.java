package com.quincus.order.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.OrderApi;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.OrderShipmentResponse;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerImplTest {

    @InjectMocks
    private OrderControllerImpl orderControllerImpl;
    @Mock
    private OrderApi orderApi;
    @Mock
    private MessageApi messageApi;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void createFromOrder_ValidData_ShouldReturnSuccess() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId("journey=1");
        List<Shipment> shipments = new ArrayList<>();
        Shipment shipmentRs1 = new Shipment();
        shipmentRs1.setId("shipment-1");
        shipmentRs1.setShipmentJourney(journey);
        Shipment shipmentRs2 = new Shipment();
        shipmentRs2.setId("shipment-2");
        shipmentRs2.setShipmentJourney(journey);
        shipments.add(shipmentRs1);
        shipments.add(shipmentRs2);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(shipments);
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);
        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        Response<List<Shipment>> response = orderControllerImpl.createFromOrder(request);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0)).isNotNull();
        assertThat(response.getData().get(1)).isNotNull();
        assertThat(response.getErrors()).isNull();

        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                eq(SegmentDispatchType.SHIPMENT_CREATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentWithJourneyToQShip(any(Shipment.class),
                any(ShipmentJourney.class));
    }

    @Test
    void createFromOrder_ValidDataUpdated_ShouldReturnSuccess() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId("journey=1");
        List<Shipment> shipments = new ArrayList<>();
        Shipment shipmentRs1 = new Shipment();
        shipmentRs1.setId("shipment-1");
        shipmentRs1.setShipmentJourney(journey);
        Shipment shipmentRs2 = new Shipment();
        shipmentRs2.setId("shipment-2");
        shipmentRs2.setShipmentJourney(journey);
        shipmentRs2.setUpdated(true);
        shipments.add(shipmentRs1);
        shipments.add(shipmentRs2);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(shipments);
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);
        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        Response<List<Shipment>> response = orderControllerImpl.createFromOrder(request);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0)).isNotNull();
        assertThat(response.getData().get(1)).isNotNull();
        assertThat(response.getErrors()).isNull();

        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                eq(SegmentDispatchType.SHIPMENT_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentWithJourneyToQShip(any(Shipment.class),
                any(ShipmentJourney.class));
    }

    @Test
    void createFromOrder_ValidDataCancelledStatus_ShouldReturnSuccess() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId("journey=1");
        List<Shipment> shipments = new ArrayList<>();
        Shipment shipmentRs1 = new Shipment();
        shipmentRs1.setId("shipment-1");
        shipmentRs1.setShipmentJourney(journey);
        Shipment shipmentRs2 = new Shipment();
        shipmentRs2.setId("shipment-2");
        shipmentRs2.setShipmentJourney(journey);
        shipments.add(shipmentRs1);
        shipments.add(shipmentRs2);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(shipments);
        when(orderApi.isOrderNotCancelled(any())).thenReturn(false);
        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        Response<List<Shipment>> response = orderControllerImpl.createFromOrder(request);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0)).isNotNull();
        assertThat(response.getData().get(1)).isNotNull();
        assertThat(response.getErrors()).isNull();

        verify(messageApi, times(0)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                eq(SegmentDispatchType.SHIPMENT_CREATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentWithJourneyToQShip(any(Shipment.class),
                any(ShipmentJourney.class));
    }

    @Test
    void createFromOrder_ValidDataSegmentUpdated_ShouldReturnSuccess() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId("journey=1");
        List<Shipment> shipments = new ArrayList<>();
        Shipment shipmentRs1 = new Shipment();
        shipmentRs1.setId("shipment-1");
        shipmentRs1.setShipmentJourney(journey);
        Shipment shipmentRs2 = new Shipment();
        shipmentRs2.setId("shipment-2");
        shipmentRs2.setShipmentJourney(journey);
        shipmentRs2.setUpdated(true);
        shipmentRs2.setSegmentsUpdatedFromSource(true);
        shipments.add(shipmentRs1);
        shipments.add(shipmentRs2);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(shipments);
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);
        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        Response<List<Shipment>> response = orderControllerImpl.createFromOrder(request);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0)).isNotNull();
        assertThat(response.getData().get(1)).isNotNull();
        assertThat(response.getErrors()).isNull();

        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentWithJourneyToQShip(any(Shipment.class),
                any(ShipmentJourney.class));
    }

    @Test
    void createFromOrder_noShipments_shouldThrowError() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        Order orderDummy = new Order();
        orderDummy.setId("order-1");

        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        when(orderApi.createOrderFromPayload(any())).thenReturn(orderDummy);
        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> orderControllerImpl.createFromOrder(request))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void createFromOrder_InValidData_ShouldReturnSuccess() {
        Request<Object> request = new Request<>();
        request.setData("root");
        doThrow(new RuntimeException("Conversion exception. Test only."))
                .when(orderApi).createOrUpdateShipments(any(), anyString(), anyString());
        assertThatThrownBy(() -> orderControllerImpl.createFromOrder(request))
                .isInstanceOf(RuntimeException.class);

    }

    @Test
    void asyncCreateFromOrder_ValidData_ShouldReturnSuccess() throws JsonProcessingException {
        Request<Object> request = new Request<>();
        request.setData("root");
        String orderId = UUID.randomUUID().toString();
        String orderIdLabel = UUID.randomUUID().toString();

        when(orderApi.asyncCreateOrUpdateShipments(any(), anyString(), anyString())).thenReturn(new OrderShipmentResponse(orderId, orderIdLabel, List.of()));
        when(objectMapper.writeValueAsString(any(Object.class))).thenReturn("");
        Response<OrderShipmentResponse> response = orderControllerImpl.asyncCreateFromOrder(request);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isNull();
    }
}
