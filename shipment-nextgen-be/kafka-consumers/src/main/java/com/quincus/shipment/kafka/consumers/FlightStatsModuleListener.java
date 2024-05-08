package com.quincus.shipment.kafka.consumers;

import com.quincus.shipment.api.FlightStatsApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.exception.FlightStatsMessageException;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.generateUUIDForConsumerRecord;
import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.logConsumerRecordDetails;

@Component
@Slf4j
@AllArgsConstructor
public class FlightStatsModuleListener {
    private static final String FLIGHT_STATS_MESSAGE_TO_PARSE = "Flight stats message to parse `{}` ";
    private static final String ERROR_PARSING_FLIGHT_STATS_MESSAGE = "Error parsing Flight Stats message {} ";
    private static final String ERROR_FAILED_CONSUMING_MESSAGE_FROM_FLIGHT_STATS = "Something went wrong while consuming message from Flight Stats {} ";
    private static final String INFO_UPDATED_FLIGHT_DETAILS = "Updated flight details with flight id: {}";
    private final FlightStatsApi flightStatsApi;

    @KafkaListener(
            topics = {"#{kafkaInboundTopicProperties.getFlightStatsInboundTopic()}"},
            containerFactory = "flightStatsKafkaListenerContainerFactory",
            autoStartup = "#{kafkaFlightStatsConsumerProperties.isEnable()}"
    )
    @LogExecutionTime
    public void listen(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord == null) {
            log.warn("Received null consumer record from Flight Stats Module.");
            return;
        }
        String transactionId = generateUUIDForConsumerRecord(consumerRecord);
        try {
            logConsumerRecordDetails("Flight Stats Module", consumerRecord, transactionId);
            String message = consumerRecord.value();
            log.info(FLIGHT_STATS_MESSAGE_TO_PARSE, message);

            flightStatsApi.receiveFlightStatsMessage(message, transactionId);
            log.info(INFO_UPDATED_FLIGHT_DETAILS, transactionId);

        } catch (FlightStatsMessageException e) {
            log.error(ERROR_PARSING_FLIGHT_STATS_MESSAGE, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.FLIGHT_STATS_MODULE, e.getMessage(), transactionId);
        } catch (Exception e) {
            log.error(ERROR_FAILED_CONSUMING_MESSAGE_FROM_FLIGHT_STATS, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.FLIGHT_STATS_MODULE, e.getMessage(), transactionId);
        }
    }
}
