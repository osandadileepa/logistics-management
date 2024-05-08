package com.quincus.order.web.api;

import com.amazonaws.services.outposts.model.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.OrderShipmentResponse;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApiImplTest {

    @InjectMocks
    private OrderApiImpl orderApi;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ShipmentApi shipmentApi;

    @Test
    void createOrderFromPayload_ShouldReturnOrderParseFromObjectMapper() throws JsonProcessingException {
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode idJsonNode = mock(JsonNode.class);
        JsonNode statusJsonNode = mock(JsonNode.class);
        JsonNode trackingUrlJsonNode = mock(JsonNode.class);
        JsonNode segmentsUpdatedJsonNode = mock(JsonNode.class);
        JsonNode usedOpenApiJsonNode = mock(JsonNode.class);
        JsonNode orderIdLabelNode = mock(JsonNode.class);
        JsonNode createdAtJsonNode = mock(JsonNode.class);
        JsonNode orderReferenceArrayNode = mock(ArrayNode.class);


        String orderPayload = "{\"id\":\"12345\",\"status\":\"created\",\"created_at\":\"2023-08-17T15:18:19.016Z\",\"tracking_url\":\"https://fake.com\",\"segments_updated\":false,\"used_open_api\":false}";

        when(objectMapper.readTree(orderPayload)).thenReturn(jsonNode);
        when(jsonNode.get("id")).thenReturn(idJsonNode);
        when(idJsonNode.asText()).thenReturn("12345");
        when(jsonNode.get("status")).thenReturn(statusJsonNode);
        when(statusJsonNode.asText()).thenReturn(Root.STATUS_CREATED);
        when(jsonNode.get("created_at")).thenReturn(createdAtJsonNode);
        when(createdAtJsonNode.asText()).thenReturn("2023-08-17T15:18:19.016Z");
        when(jsonNode.get("tracking_url")).thenReturn(trackingUrlJsonNode);
        when(trackingUrlJsonNode.asText()).thenReturn("https://fake.com");
        when(jsonNode.get("segments_updated")).thenReturn(segmentsUpdatedJsonNode);
        when(segmentsUpdatedJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("used_open_api")).thenReturn(usedOpenApiJsonNode);
        when(usedOpenApiJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("order_id_label")).thenReturn(orderIdLabelNode);
        when(orderIdLabelNode.asText()).thenReturn("QR11330511234");
        when(jsonNode.get("order_references")).thenReturn(orderReferenceArrayNode);

        Order order = orderApi.createOrderFromPayload(orderPayload);

        verify(objectMapper, times(1)).readTree(orderPayload);
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("12345");
        assertThat(order.getStatus()).isEqualTo(Root.STATUS_CREATED);
        assertThat(order.getTimeCreated()).isEqualTo("2023-08-17T15:18:19.016Z");
        assertThat(order.getTrackingUrl()).isEqualTo("https://fake.com");
        assertThat(order.isSegmentsUpdated()).isFalse();
        assertThat(order.isUsedOpenApi()).isFalse();
    }

    @Test
    void validPayload_createOrUpdateShipmentsForLocal_ShouldCallCreateOrUpdate() {

        String uuid = UUID.randomUUID().toString();
        String orderPayload = "{\"id\":\"12345\"}";

        Order order = mock(Order.class);
        when(order.isSegmentsUpdated()).thenReturn(true);
        List<Shipment> shipmentCreated = new ArrayList<>();

        Shipment shipment = new Shipment();
        shipment.setId("1");
        shipment.setShipmentTrackingId("t1");
        Shipment shipment2 = new Shipment();
        shipment2.setId("2");
        shipment2.setShipmentTrackingId("t2");

        shipmentCreated.add(shipment);
        shipmentCreated.add(shipment2);

        when(shipmentApi.convertOrderMessageToShipments(anyString(), anyString())).thenReturn(shipmentCreated);
        when(shipmentApi.createOrUpdate(shipmentCreated, true)).thenReturn(shipmentCreated);

        //WHEN:
        List<Shipment> result = orderApi.createOrUpdateShipments(order, orderPayload, uuid);

        //THEN:
        verify(shipmentApi, times(1)).createOrUpdate(anyList(), eq(true));
        assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    void shipmentFromOrderNotPresent_createOrUpdateShipmentsForLocal_shipmentShouldBeCancel() {

        String uuid = UUID.randomUUID().toString();
        String orderPayload = "{\"id\":\"12345\"}";

        Order order = mock(Order.class);
        List<Shipment> shipmentCreated = new ArrayList<>();
        when(order.isSegmentsUpdated()).thenReturn(true);

        Shipment shipment = new Shipment();
        shipment.setId("1");
        shipment.setShipmentTrackingId("t1");

        shipmentCreated.add(shipment);

        when(shipmentApi.convertOrderMessageToShipments(orderPayload, uuid)).thenReturn(shipmentCreated);
        when(shipmentApi.createOrUpdate(anyList(), eq(true))).thenReturn(List.of(shipment));

        List<Shipment> shipmentsInOrder = new ArrayList<>();

        Shipment shipment2 = new Shipment();
        shipment2.setId("2");
        shipment2.setShipmentTrackingId("t2");
        shipmentsInOrder.add(shipment);
        shipmentsInOrder.add(shipment2);
        when(shipmentApi.findAllRelatedFromOrder(order)).thenReturn(shipmentsInOrder);

        //WHEN:
        List<Shipment> result = orderApi.createOrUpdateShipments(order, orderPayload, uuid);

        //THEN:
        verify(shipmentApi, times(shipmentCreated.size())).createOrUpdate(any(), eq(true));
        assertThat(result).isNotNull().hasSize(1);
        verify(shipmentApi, times(1)).cancel(shipment2.getId(), TriggeredFrom.OM);
    }

    @Test
    void shipmentFromOrderNotPresent_asyncCreateOrUpdateShipments_shipmentShouldBeCancel() {

        String uuid = UUID.randomUUID().toString();
        String orderPayload = "{\"id\":\"12345\"}";

        Order order = mock(Order.class);
        List<Shipment> shipmentCreated = new ArrayList<>();
        when(order.isSegmentsUpdated()).thenReturn(true);

        Shipment shipment = new Shipment();
        shipment.setId("1");
        shipment.setShipmentTrackingId("t1");

        shipmentCreated.add(shipment);

        when(shipmentApi.convertOrderMessageToShipments(orderPayload, uuid)).thenReturn(shipmentCreated);

        List<Shipment> shipmentsInOrder = new ArrayList<>();

        Shipment shipment2 = new Shipment();
        shipment2.setId("2");
        shipment2.setShipmentTrackingId("t2");
        shipmentsInOrder.add(shipment);
        shipmentsInOrder.add(shipment2);
        when(shipmentApi.findAllRelatedFromOrder(order)).thenReturn(shipmentsInOrder);

        //WHEN:
        OrderShipmentResponse result = orderApi.asyncCreateOrUpdateShipments(order, orderPayload, uuid);

        //THEN:
        verify(shipmentApi, times(shipmentCreated.size())).asyncCreateOrUpdate(any(), eq(true));
        assertThat(result).isNotNull();
    }

    @Test
    void givenSegmentsUpdatedIsMissingOnPayload_whenCreateOrderFromPayload_thenDefaultToFalse()
            throws JsonProcessingException {
        final String givenOrderPayload = "{\"id\":\"12345\"}";
        final JsonNode mockNode = mock(JsonNode.class);
        JsonNode segmentJsonNode = mock(JsonNode.class);
        when(mockNode.get(AdditionalMatchers.not(eq("segments_updated")))).thenReturn(mockNode);
        when(mockNode.get(AdditionalMatchers.not(eq("used_open_api")))).thenReturn(mockNode);
        when(objectMapper.readTree(givenOrderPayload)).thenReturn(mockNode);
        when(segmentJsonNode.asBoolean()).thenReturn(false);
        when(mockNode.get("segments_updated")).thenReturn(segmentJsonNode);
        when(mockNode.get("used_open_api")).thenReturn(segmentJsonNode);
        final Order actualOrder = orderApi.createOrderFromPayload(givenOrderPayload);

        assertThat(actualOrder.isSegmentsUpdated())
                .as("Should be 'False' by default when segments_updated is missing from payload.")
                .isFalse();
        assertThat(actualOrder.isUsedOpenApi())
                .as("Should be 'False' by default when segments_updated is missing from payload.")
                .isFalse();
    }

    @Test
    void givenSegmentsUpdatedIsTrue_whenCreateOrderFromPayload_thenEnableUpdatingShipmentJourney()
            throws JsonProcessingException {
        final String givenOrderPayload = "{\"id\":\"12345\", \"segments_updated\":true}";
        final JsonNode mockNode = mock(JsonNode.class);
        final JsonNode innerMockNode = mock(JsonNode.class);
        when(mockNode.get("segments_updated")).thenReturn(innerMockNode);
        when(mockNode.get(AdditionalMatchers.not(eq("segments_updated")))).thenReturn(mockNode);
        when(objectMapper.readTree(givenOrderPayload)).thenReturn(mockNode);
        when(innerMockNode.asBoolean()).thenReturn(true);

        final Order actualOrder = orderApi.createOrderFromPayload(givenOrderPayload);

        assertThat(actualOrder.isSegmentsUpdated())
                .as("Must be 'True' to update ShipmentJourney.")
                .isTrue();
    }

    @Test
    void givenSegmentsUpdatedIsFalse_whenCreateOrderFromPayload_thenSkipUpdatingShipmentJourney()
            throws JsonProcessingException {
        final String givenOrderPayload = "{\"id\":\"12345\", \"segments_updated\":false}";
        final JsonNode mockNode = mock(JsonNode.class);
        final JsonNode innerMockNode = mock(JsonNode.class);
        when(mockNode.get("segments_updated")).thenReturn(innerMockNode);
        when(mockNode.get(AdditionalMatchers.not(eq("segments_updated")))).thenReturn(mockNode);
        when(objectMapper.readTree(givenOrderPayload)).thenReturn(mockNode);
        when(innerMockNode.asBoolean()).thenReturn(false);

        final Order actualOrder = orderApi.createOrderFromPayload(givenOrderPayload);

        assertThat(actualOrder.isSegmentsUpdated())
                .as("Must be 'False' to skip updating ShipmentJourney.")
                .isFalse();
    }

    @Test
    void givenOrderCreatedStatus_whenCreateOrUpdateShipments_thenProcessOrder() {
        final Order order = new Order();
        order.setStatus(Root.STATUS_CREATED);

        final String givenOrderPayload = "{\"id\":\"12345\", \"status\":Ordered}";

        orderApi.createOrUpdateShipments(order, givenOrderPayload, "123");

        verify(shipmentApi).convertOrderMessageToShipments(any(), any());
    }

    @Test
    void givenOrderDraftStatus_whenCreateOrUpdateShipments_thenThrowOperationNotAllowedExeption() {
        final Order order = new Order();
        order.setStatus("draft");

        final String givenOrderPayload = "{\"id\":\"12345\", \"status\":draft}";

        assertThrows(QuincusValidationException.class,
                () -> orderApi.createOrUpdateShipments(order, givenOrderPayload, "123"));
    }

    @Test
    void givenOrderCreatedStatus_whenAsyncCreateOrUpdateShipments_thenProcessOrder() {
        final Order order = new Order();
        order.setStatus(Root.STATUS_CREATED);

        final String givenOrderPayload = "{\"id\":\"12345\", \"status\":created}";

        orderApi.asyncCreateOrUpdateShipments(order, givenOrderPayload, "123");

        verify(shipmentApi).convertOrderMessageToShipments(any(), any());
    }

    @Test
    void givenOrderDraftStatus_whenAsyncCreateOrUpdateShipments_thenThrowOperationNotAllowedExeption() {
        final Order order = new Order();
        order.setStatus("draft");

        final String givenOrderPayload = "{\"id\":\"12345\", \"status\":draft}";

        assertThrows(QuincusValidationException.class,
                () -> orderApi.asyncCreateOrUpdateShipments(order, givenOrderPayload, "123"));
    }

    @Test
    void givenOrderCreatedStatus_whenCreateOrUpdateShipmentsLocal_thenProcessOrder() throws JsonProcessingException {
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode idJsonNode = mock(JsonNode.class);
        JsonNode statusJsonNode = mock(JsonNode.class);
        JsonNode trackingUrlJsonNode = mock(JsonNode.class);
        JsonNode segmentsUpdatedJsonNode = mock(JsonNode.class);
        JsonNode usedOpenApiJsonNode = mock(JsonNode.class);
        JsonNode orderIdLabelNode = mock(JsonNode.class);
        JsonNode orderReferenceArrayNode = mock(ArrayNode.class);

        String orderPayload = "{\"id\":\"12345\",\"status\":\"created\",\"tracking_url\":\"https://fake.com\",\"segments_updated\":false,\"used_open_api\":false}";

        when(objectMapper.readTree(orderPayload)).thenReturn(jsonNode);
        when(jsonNode.get("id")).thenReturn(idJsonNode);
        when(idJsonNode.asText()).thenReturn("12345");
        when(jsonNode.get("status")).thenReturn(statusJsonNode);
        when(statusJsonNode.asText()).thenReturn(Root.STATUS_CREATED);
        when(jsonNode.get("tracking_url")).thenReturn(trackingUrlJsonNode);
        when(trackingUrlJsonNode.asText()).thenReturn("https://fake.com");
        when(jsonNode.get("segments_updated")).thenReturn(segmentsUpdatedJsonNode);
        when(segmentsUpdatedJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("used_open_api")).thenReturn(usedOpenApiJsonNode);
        when(usedOpenApiJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("order_id_label")).thenReturn(orderIdLabelNode);
        when(orderIdLabelNode.asText()).thenReturn("QR11330511234");
        when(jsonNode.get("order_references")).thenReturn(orderReferenceArrayNode);

        orderApi.createOrUpdateShipmentsLocal(orderPayload, false);

        verify(shipmentApi).convertOrderMessageToShipments(any(), any());
    }

    @Test
    void givenOrderDraftStatus_whenCreateOrUpdateShipmentsLocal_thenThrowOperationNotAllowedExeption() throws JsonProcessingException {
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode idJsonNode = mock(JsonNode.class);
        JsonNode statusJsonNode = mock(JsonNode.class);
        JsonNode trackingUrlJsonNode = mock(JsonNode.class);
        JsonNode segmentsUpdatedJsonNode = mock(JsonNode.class);
        JsonNode usedOpenApiJsonNode = mock(JsonNode.class);
        JsonNode orderIdLabelNode = mock(JsonNode.class);
        JsonNode orderReferenceArrayNode = mock(ArrayNode.class);

        String orderPayload = "{\"id\":\"12345\",\"status\":\"draft\",\"tracking_url\":\"https://fake.com\",\"segments_updated\":false,\"used_open_api\":false}";

        when(objectMapper.readTree(orderPayload)).thenReturn(jsonNode);
        when(jsonNode.get("id")).thenReturn(idJsonNode);
        when(idJsonNode.asText()).thenReturn("12345");
        when(jsonNode.get("status")).thenReturn(statusJsonNode);
        when(statusJsonNode.asText()).thenReturn("draft");
        when(jsonNode.get("tracking_url")).thenReturn(trackingUrlJsonNode);
        when(trackingUrlJsonNode.asText()).thenReturn("https://fake.com");
        when(jsonNode.get("segments_updated")).thenReturn(segmentsUpdatedJsonNode);
        when(segmentsUpdatedJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("used_open_api")).thenReturn(usedOpenApiJsonNode);
        when(usedOpenApiJsonNode.asBoolean()).thenReturn(false);
        when(jsonNode.get("order_id_label")).thenReturn(orderIdLabelNode);
        when(orderIdLabelNode.asText()).thenReturn("QR11330511234");
        when(jsonNode.get("order_references")).thenReturn(orderReferenceArrayNode);

        assertThrows(QuincusValidationException.class,
                () -> orderApi.createOrUpdateShipmentsLocal(orderPayload, false));
    }

    @Test
    void givenOrderCancelledStatus_whenIsOrderNotCancelled_thenReturnFalse() throws JsonProcessingException {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED.name());

        boolean result = orderApi.isOrderNotCancelled(order);

        assertFalse(orderApi.isOrderNotCancelled(order));
    }

    @Test
    void givenOrderCancelledStatus_whenIsOrderNotCancelled_thenReturnTrue() throws JsonProcessingException {
        Order order = new Order();
        order.setStatus(OrderStatus.COMPLETED.name());

        boolean result = orderApi.isOrderNotCancelled(order);

        assertTrue(orderApi.isOrderNotCancelled(order));
    }
}
