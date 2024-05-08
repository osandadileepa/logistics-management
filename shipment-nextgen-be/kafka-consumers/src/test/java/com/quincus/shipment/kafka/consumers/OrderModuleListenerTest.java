package com.quincus.shipment.kafka.consumers;

import com.amazonaws.services.outposts.model.OrderStatus;
import com.quincus.order.api.OrderApi;
import com.quincus.order.integration.api.OrderApiIntegration;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Collections;

import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.generateUUIDForConsumerRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderModuleListenerTest {

    @InjectMocks
    private OrderModuleListener listener;

    @Mock
    private MessageApi messageApi;

    @Mock
    private OrderApiIntegration orderApiIntegration;

    @Mock
    private OrderApi orderApi;

    @Mock
    private Acknowledgment ack;

    @BeforeEach
    void setUp() {
        Order order = new Order();
        order.setId("123");
        order.setStatus("status");
        order.setSegmentsUpdated(false);
        order.setUsedOpenApi(false);
        when(orderApi.createOrderFromPayload(anyString())).thenReturn(order);
        lenient().doNothing().when(ack).acknowledge();
    }

    @Test
    void testListen() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        String transactionId = generateUUIDForConsumerRecord(record);

        Shipment shipment = new Shipment();

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(Collections.singletonList(shipment));
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);

        listener.listen(record, ack);

        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentToQShip(any(Shipment.class));
        verify(orderApiIntegration, times(0)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();
        assertThat(transactionId).isNotBlank();
    }

    @Test
    void testListen_GeneralExceptionThrown() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenThrow(new RuntimeException("RuntimeException"));

        assertThatThrownBy(() -> listener.listen(record, ack))
                .isInstanceOf(KafkaConsumerException.class);

        verify(messageApi, times(0)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(orderApiIntegration, times(1)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();
    }

    @Test
    void testListen_SegmentExceptionThrown() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenThrow(new SegmentException("SegmentException"));

        assertThatThrownBy(() -> listener.listen(record, ack))
                .isInstanceOf(KafkaConsumerException.class);

        verify(messageApi, times(0)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(orderApiIntegration, times(1)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();
    }

    @Test
    void testListen_ShipmentUpdated() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Order order = new Order();
        order.setId("123");
        order.setStatus("status");
        Shipment shipment = new Shipment();
        shipment.setUpdated(true);
        shipment.setSegmentUpdated(false);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(Collections.singletonList(shipment));
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);

        listener.listen(record, ack);

        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(orderApiIntegration, times(0)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();
    }

    @Test
    void testListen_WithOpenAPIAsFalse() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Order order = new Order();
        order.setId("123");
        order.setStatus("status");
        order.setUsedOpenApi(Boolean.FALSE);
        when(orderApi.createOrderFromPayload(anyString())).thenReturn(order);
        Shipment shipment = new Shipment();
        shipment.setUpdated(true);
        shipment.setSegmentUpdated(false);

        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(Collections.singletonList(shipment));
        when(orderApi.isOrderNotCancelled(any())).thenReturn(true);

        listener.listen(record, ack);
        verify(messageApi, times(1)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(orderApiIntegration, times(0)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();
    }

    @Test
    void testListen_WithOpenAPIAsTrue() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Order order = new Order();
        order.setId("123");
        order.setStatus("status");
        order.setUsedOpenApi(Boolean.TRUE);
        when(orderApi.createOrderFromPayload(anyString())).thenReturn(order);

        listener.listen(record, ack);
        verify(messageApi, times(0)).sendSegmentDispatch(anyList(), any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(orderApiIntegration, times(0)).rollback(anyString(), anyString());
        verify(ack, times(1)).acknowledge();

    }

    @Test
    void testListen_WithDraftStatus() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Order order = new Order();
        order.setId("123");
        order.setStatus("Draft");
        order.setUsedOpenApi(Boolean.FALSE);
        when(orderApi.createOrderFromPayload(anyString())).thenReturn(order);

        listener.listen(record, ack);
        verify(orderApi, times(0)).createOrUpdateShipments(any(), any(), any());
        verify(messageApi, times(0)).sendSegmentDispatch(any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(0)).sendShipmentToQShip(any());
        verify(ack, times(1)).acknowledge();
    }
    
    @Test
    void testListen_WithCancelledStatus() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Order order = new Order();
        order.setId("123");
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUsedOpenApi(Boolean.FALSE);

        when(orderApi.createOrderFromPayload(anyString())).thenReturn(order);
        when(orderApi.createOrUpdateShipments(any(), anyString(), anyString())).thenReturn(Collections.singletonList(new Shipment()));

        listener.listen(record, ack);
        verify(orderApi, times(1)).createOrUpdateShipments(any(), any(), any());
        verify(messageApi, times(0)).sendSegmentDispatch(any(), any(), eq(DspSegmentMsgUpdateSource.CLIENT));
        verify(messageApi, times(1)).sendShipmentToQShip(any(Shipment.class));
        verify(ack, times(1)).acknowledge();
    }
}