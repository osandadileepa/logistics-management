package com.quincus.shipment.kafka.consumers.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.FailedMessageApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.dto.FailedMessage;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class KafkaErrorHandler implements CommonErrorHandler {
    private final ObjectMapper objectMapper;
    private final FailedMessageApi failedMessageApi;

    @Override
    public void handleRecord(@NonNull Exception thrownException,
                             @NonNull ConsumerRecord<?, ?> consumerRecord,
                             @NonNull Consumer<?, ?> consumer,
                             MessageListenerContainer container) {
        log.error("Error in group: `{}`. Reason: `{}`", container.getGroupId(), thrownException.getMessage());

        if (!(thrownException.getCause() instanceof KafkaConsumerException kafkaConsumerException)) {
            log.warn("Skipping exception: `{}` to store in DLQ", thrownException.getCause().toString());
            return;
        }
        logRecordDetails(consumerRecord);
        send(consumerRecord, kafkaConsumerException.getUuid(), kafkaConsumerException.getModuleErrorCode(), kafkaConsumerException.getMessage());
    }


    private void send(ConsumerRecord<?, ?> consumerRecord, String transactionId, KafkaModuleErrorCode kafkaModuleErrorCode, String errorMessage) {
        try {
            JsonNode data = objectMapper.readTree(consumerRecord.value().toString());
            failedMessageApi.sendToDlq(buildErrorMessage(data, transactionId, kafkaModuleErrorCode, errorMessage));
        } catch (Exception e) {
            log.error("Error occurred while sending failed message", e);
        }
    }

    private FailedMessage buildErrorMessage(JsonNode data, String transactionId, KafkaModuleErrorCode kafkaModuleErrorCode, String errorMessage) {
        return new FailedMessage(
                transactionId,
                LocalDateTime.now(Clock.systemUTC()),
                data,
                kafkaModuleErrorCode,
                errorMessage);
    }

    private void logRecordDetails(ConsumerRecord<?, ?> consumerRecord) {
        log.error("Sending record to DLQ. Original topic: `{}`, Partition: `{}`, Offset: `{}`, Key: `{}`",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.key());
    }

}