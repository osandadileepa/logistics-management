package com.quincus.shipment.kafka.consumers;

import com.quincus.shipment.api.LocationApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.exception.InvalidLocationException;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthentication;
import com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.logConsumerRecordDetails;

@Component
@Slf4j
@AllArgsConstructor
public class LocationListener {
    private static final String LOCATION_MESSAGE_TO_PARSE = "Location message to parse '{}' ";
    private static final String ERROR_PARSING_LOCATION_MESSAGE = "Error parsing Location message '{}' ";
    private static final String ERROR_FAILED_CONSUMING_MESSAGE_FROM_QPORTAL = "Something went wrong while consuming message from qportal '{}' ";
    private static final String INFO_LOCATION_DETAILS = "Received location details with transaction id: '{}'";
    private final LocationApi locationApi;

    @KafkaPreAuthentication
    @KafkaListener(
            topics = {"#{kafkaInboundTopicProperties.getQportalLocationTopic()}"},
            containerFactory = "locationsKafkaListenerContainerFactory",
            autoStartup = "#{kafkaLocationsConsumerProperties.isEnable()}"
    )
    @LogExecutionTime
    public void listen(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord == null) {
            log.warn("Received null consumer record from QPortal Location Module.");
            return;
        }
        String transactionId = KafkaConsumerUtils.generateUUIDForConsumerRecord(consumerRecord);
        try {
            logConsumerRecordDetails("QPortal Location Module", consumerRecord, transactionId);
            String message = consumerRecord.value();
            log.debug(LOCATION_MESSAGE_TO_PARSE, message);

            locationApi.receiveLocationMessage(message, transactionId);
            log.debug(INFO_LOCATION_DETAILS, transactionId);

        } catch (InvalidLocationException e) {
            log.error(ERROR_PARSING_LOCATION_MESSAGE, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.LOCATION_MODULE, e.getMessage(), transactionId);
        } catch (Exception e) {
            log.error(ERROR_FAILED_CONSUMING_MESSAGE_FROM_QPORTAL, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.LOCATION_MODULE, e.getMessage(), transactionId);
        }
    }

}
