package com.quincus.shipment.kafka.consumers;

import com.quincus.order.api.OrderApi;
import com.quincus.order.integration.api.OrderApiIntegration;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.kafka.consumers.exception.KafkaConsumerException;
import com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthentication;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.generateUUIDForConsumerRecord;
import static com.quincus.shipment.kafka.consumers.utility.KafkaConsumerUtils.logConsumerRecordDetails;

@Component
@Slf4j
@AllArgsConstructor
public class OrderModuleListener {
    public static final String GENERIC_ORDER_ERROR = "Unable to process order id `{}` with transaction id `{}` due to error: `{}`";
    private static final String ORDER_RECORD_TO_PARSE = "Order Record to parse `{}` ";
    private static final String SHIPMENT_PROCESSED = "Shipment Processed `{}` ";
    private static final String SKIPPED_LOG = "RollbackAPI Skipped. Order Id : `{}`";
    private static final String SKIPPED_PROCESSING_OF_ORDER_ID = "Order processing is skipped `{}`";
    private static final String DRAFT_STATUS = "draft";
    private static final String NEW_LINE_REPLACE_STR = "\r\n";
    private static final String EMPTY_STR = "";
    private final MessageApi messageApi;
    private final OrderApiIntegration orderApiIntegration;
    private final OrderApi orderApi;

    @KafkaPreAuthentication
    @KafkaListener(
            topics = {"#{kafkaInboundTopicProperties.getOrderModuleTopic()}"},
            containerFactory = "ordersKafkaListenerContainerFactory",
            autoStartup = "#{kafkaOrdersConsumerProperties.isEnable()}"
    )
    @LogExecutionTime
    public void listen(ConsumerRecord<String, String> consumerRecord, Acknowledgment ack) {
        if (consumerRecord == null) {
            log.warn("Received null consumer record from Order Module.");
            return;
        }
        Order order = null;
        String transactionId = generateUUIDForConsumerRecord(consumerRecord);
        try {
            logConsumerRecordDetails("Order Module", consumerRecord, transactionId);
            String message = consumerRecord.value();
            log.info(ORDER_RECORD_TO_PARSE, message.replace(NEW_LINE_REPLACE_STR, EMPTY_STR));
            order = getOrderFromMessage(message);

            if (order.isUsedOpenApi() || isOrderDraft(order.getStatus())) {
                log.info(SKIPPED_PROCESSING_OF_ORDER_ID, order.getId());
                return;
            }

            createOrUpdateShipmentsAndLogResults(order, message, transactionId);
        } catch (Exception e) {
            String id = order != null ? order.getId() : EMPTY_STR;
            if (order != null && StringUtils.isNotEmpty(order.getId()) && !order.getStatus().equalsIgnoreCase(DRAFT_STATUS)) {
                log.error(GENERIC_ORDER_ERROR, id, transactionId, e.getMessage(), e);
                orderApiIntegration.rollback(id, e.getMessage());
            } else {
                log.error(SKIPPED_LOG, id);
            }
            throw new KafkaConsumerException(KafkaModuleErrorCode.ORDER_MODULE, e.getMessage(), transactionId);
        } finally {
            log.info("Done committing transaction id: `{}` for Order Module", transactionId);
            ack.acknowledge();
        }
    }

    private Order getOrderFromMessage(String message) {
        return orderApi.createOrderFromPayload(message);
    }

    private void createOrUpdateShipmentsAndLogResults(final Order order, final String msg, final String transactionId) {
        //TODO delegate this method soon to a common class affected classes: [OrderControllerImpl, OrderModuleListener, ShipmentAsyncService]
        List<Shipment> createdShipments = orderApi.createOrUpdateShipments(order, msg, transactionId);
        createdShipments.forEach(createdShipment -> log.info(SHIPMENT_PROCESSED, createdShipment.getId()));

        SegmentDispatchType segmentDispatchType;
        if (createdShipments.stream().anyMatch(Shipment::isSegmentsUpdatedFromSource)) {
            segmentDispatchType = SegmentDispatchType.JOURNEY_UPDATED;
        } else if (createdShipments.stream().anyMatch(Shipment::isUpdated)) {
            segmentDispatchType = SegmentDispatchType.SHIPMENT_UPDATED;
        } else {
            segmentDispatchType = SegmentDispatchType.SHIPMENT_CREATED;
        }

        if (orderApi.isOrderNotCancelled(order)) {
            messageApi.sendSegmentDispatch(createdShipments, createdShipments.get(0).getShipmentJourney(), segmentDispatchType, DspSegmentMsgUpdateSource.CLIENT);
        }
        messageApi.sendShipmentToQShip(createdShipments.get(0));
    }

    private boolean isOrderDraft(final String status) {
        return DRAFT_STATUS.equalsIgnoreCase(status);
    }
}
