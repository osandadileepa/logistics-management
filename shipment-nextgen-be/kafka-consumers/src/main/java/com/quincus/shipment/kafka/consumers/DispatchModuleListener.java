package com.quincus.shipment.kafka.consumers;

import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.InvalidMilestoneException;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthentication;
import com.quincus.web.common.exception.model.QuincusValidationException;
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
public class DispatchModuleListener {
    private static final String MILESTONE_MESSAGE_TO_PARSE = "Milestone message to parse {} ";
    private static final String ERROR_PARSING_MILESTONE_MESSAGE = "Error parsing Milestone message {} ";
    private static final String ERROR_FAILED_CONSUMING_MESSAGE_FROM_DISPATCH = "Something went wrong while consuming message from dispatch {} ";
    private final ShipmentApi shipmentApi;
    private final MilestonePostProcessApi milestonePostProcessApi;

    @KafkaPreAuthentication
    @KafkaListener(
            topics = {"#{kafkaInboundTopicProperties.getDispatchModuleMilestoneTopic()}"},
            containerFactory = "dispatchMilestoneKafkaListenerContainerFactory",
            autoStartup = "#{kafkaDispatchMilestoneConsumerProperties.isEnable()}"
    )
    @LogExecutionTime
    public void listen(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord == null) {
            log.warn("Received null consumer record from Dispatch/DSP Module.");
            return;
        }
        String transactionId = generateUUIDForConsumerRecord(consumerRecord);

        try {
            logConsumerRecordDetails("Dispatch/DSP Module", consumerRecord, transactionId);
            String message = consumerRecord.value();
            log.info(MILESTONE_MESSAGE_TO_PARSE, message);

            Milestone milestone = shipmentApi.receiveMilestoneMessageFromDispatch(message, transactionId);
            if (milestone == null) {
                log.error(ERROR_PARSING_MILESTONE_MESSAGE, message);
                throw new QuincusValidationException(ERROR_PARSING_MILESTONE_MESSAGE, message);
            }

            Shipment shipment = shipmentApi.find(milestone.getShipmentId());

            milestonePostProcessApi.createAndSendShipmentMilestone(milestone, shipment);
            milestonePostProcessApi.createAndSendAPIGWebhooks(milestone, shipment);
            milestonePostProcessApi.createAndSendSegmentDispatch(milestone, shipment);
            milestonePostProcessApi.createAndSendQShipSegment(milestone, shipment);
            milestonePostProcessApi.createAndSendNotification(milestone, shipment);
        } catch (InvalidMilestoneException e) {
            log.error(ERROR_PARSING_MILESTONE_MESSAGE, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.DISPATCH_MODULE, e.getMessage(), transactionId);
        } catch (Exception e) {
            log.error(ERROR_FAILED_CONSUMING_MESSAGE_FROM_DISPATCH, e.getMessage(), e);
            throw new KafkaConsumerException(KafkaModuleErrorCode.DISPATCH_MODULE, e.getMessage(), transactionId);
        }
    }
}
