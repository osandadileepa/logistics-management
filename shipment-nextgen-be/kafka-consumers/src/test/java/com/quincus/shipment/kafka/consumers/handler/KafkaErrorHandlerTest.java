package com.quincus.shipment.kafka.consumers.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.FailedMessageApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.dto.FailedMessage;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.listener.MessageListenerContainer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaErrorHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FailedMessageApi failedMessageApi;
    @Mock
    private Consumer<?, ?> consumer;
    @Mock
    private MessageListenerContainer container;
    @Mock
    private JsonNode jsonNode;

    private KafkaErrorHandler kafkaErrorHandler;

    @BeforeEach
    void setUp() {
        kafkaErrorHandler = new KafkaErrorHandler(objectMapper, failedMessageApi);
    }

    @Test
    void testHandleRecord_withKafkaConsumerException() throws Exception {
        Exception cause = new KafkaConsumerException(KafkaModuleErrorCode.ORDER_MODULE, "test", "uuid");
        Exception thrownException = new RuntimeException("Wrapper exception", cause);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");

        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        kafkaErrorHandler.handleRecord(thrownException, record, consumer, container);

        verify(failedMessageApi).sendToDlq(any(FailedMessage.class));
    }

    @Test
    void testHandleRecord_withoutKafkaConsumerException() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");
        Exception thrownException = new RuntimeException("Wrapper exception", new RuntimeException("Inner"));

        kafkaErrorHandler.handleRecord(thrownException, record, consumer, container);

        verify(failedMessageApi, never()).sendToDlq(any(FailedMessage.class));
    }

    @Test
    void testHandleRecord_withKafkaConsumerExceptionAndReadTreeFails() throws Exception {
        Exception cause = new KafkaConsumerException(KafkaModuleErrorCode.ORDER_MODULE, "test", "uuid");
        Exception thrownException = new RuntimeException("Wrapper exception", cause);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");

        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Failed to read tree"));

        kafkaErrorHandler.handleRecord(thrownException, record, consumer, container);

        verify(failedMessageApi, never()).sendToDlq(any(FailedMessage.class));
    }

    @Test
    void testHandleRecord_whenSendToDlqFails() throws Exception {
        Exception cause = new KafkaConsumerException(KafkaModuleErrorCode.ORDER_MODULE, "test", "uuid");
        Exception thrownException = new RuntimeException("Wrapper exception", cause);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "value");

        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        doThrow(new RuntimeException("Failed to send to DLQ")).when(failedMessageApi).sendToDlq(any(FailedMessage.class));

        kafkaErrorHandler.handleRecord(thrownException, record, consumer, container);

        verify(failedMessageApi).sendToDlq(any(FailedMessage.class));
    }
}

