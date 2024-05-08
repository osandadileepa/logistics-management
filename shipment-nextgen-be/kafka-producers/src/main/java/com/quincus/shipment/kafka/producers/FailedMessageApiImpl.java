package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.FailedMessageApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.dto.FailedMessage;
import com.quincus.shipment.kafka.connection.properties.KafkaDeadLetterProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class FailedMessageApiImpl implements FailedMessageApi {

    private final KafkaDeadLetterProperties kafkaDeadLetterProperties;
    private final KafkaTemplate<String, String> dlqKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendToDlq(FailedMessage failedMessage) {
        String dlqTopic = getDlqTopicForModule(failedMessage.kafkaModuleErrorCode());
        String serializedDLQMessage = failedMessageToString(failedMessage);
        Optional.ofNullable(serializedDLQMessage)
                .ifPresent(message ->
                        dlqKafkaTemplate.send(new ProducerRecord<>(dlqTopic, failedMessage.transactionId(), message)));
    }

    private String failedMessageToString(FailedMessage failedMessage) {
        try {
            return objectMapper.writeValueAsString(failedMessage);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while parsing FailedMessage `{}` to string", failedMessage, e);
            return null;
        }
    }

    private String getDlqTopicForModule(KafkaModuleErrorCode moduleErrorCode) {
        return switch (moduleErrorCode) {
            case ORDER_MODULE -> kafkaDeadLetterProperties.getOrderModuleDlqTopic();
            case FLIGHT_STATS_MODULE -> kafkaDeadLetterProperties.getFlightStatsInboundDlqTopic();
            case LOCATION_MODULE -> kafkaDeadLetterProperties.getQportalLocationDlqTopic();
            case DISPATCH_MODULE -> kafkaDeadLetterProperties.getDispatchModuleMilestoneDlqTopic();
        };
    }
}
