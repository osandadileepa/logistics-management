package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.dto.FailedMessage;
import com.quincus.shipment.kafka.connection.properties.KafkaDeadLetterProperties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedMessageApiImplTest {

    @Mock
    private KafkaDeadLetterProperties kafkaDeadLetterProperties;

    @Mock
    private KafkaTemplate<String, String> dlqKafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FailedMessageApiImpl failedMessageApiImpl;

    private FailedMessage failedMessage;

    @BeforeEach
    void setUp() {
        failedMessage = new FailedMessage("transactionId", null, null, KafkaModuleErrorCode.ORDER_MODULE, "errorMessage");
    }

    @Test
    void testSendToDlq_ValidDlqTopic() throws JsonProcessingException {
        when(kafkaDeadLetterProperties.getOrderModuleDlqTopic()).thenReturn("sampleDlqTopic");
        when(objectMapper.writeValueAsString(failedMessage)).thenReturn("serializedMessage");

        failedMessageApiImpl.sendToDlq(failedMessage);

        verify(dlqKafkaTemplate).send(new ProducerRecord<>("sampleDlqTopic", "transactionId", "serializedMessage"));
    }

    @Test
    void testSendToDlq_NoDlqTopicFound() {
        when(kafkaDeadLetterProperties.getOrderModuleDlqTopic()).thenReturn(null);

        failedMessageApiImpl.sendToDlq(failedMessage);

        verify(dlqKafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void testSendToDlq_ErrorWhileSerializingFailedMessage() throws JsonProcessingException {
        when(kafkaDeadLetterProperties.getOrderModuleDlqTopic()).thenReturn("sampleDlqTopic");
        when(objectMapper.writeValueAsString(failedMessage)).thenThrow(new JsonProcessingException("") {});

        failedMessageApiImpl.sendToDlq(failedMessage);

        verify(dlqKafkaTemplate, never()).send(any(ProducerRecord.class));
    }

}
