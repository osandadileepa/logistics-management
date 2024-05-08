package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.connection.properties.KafkaDebugProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOutboundTopicProperties;
import com.quincus.shipment.kafka.producers.helper.CreateMessageHelper;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

@Service
@Slf4j
@AllArgsConstructor
public class MessageService {

    public static final String DEFAULT_MDC_KEY = "shp-nextgen";
    public static final String ERROR_OBJECT_KAFKA_SERIALIZATION = "Error occurred while writing object `{}` as json text. Message not sent to Kafka.";
    public static final String ERROR_IN_MAPPING_SENDING_MESSAGE = "Error in mapping/sending message: ";
    private static final String MDC_UUID = "UUID";
    private static final String DEBUG_SENDING_MESSAGE_QSHIP_SEGMENT_ID = "Sending message `{}` to QShip.";
    private static final String INFO_MESSAGE_WITH_UUID_HEADER_TO_TOPIC_SENT = "Sent message with UUID header `{}` and key `{}` to topic `{}`";
    private static final String ERROR_MESSAGE_ON_KAFKA = "Error encountered while sending `{}` to `{}`. Error message: `{}`";
    private static final String DEBUG_MESSAGE_SENT_TO_KAFKA = "Successfully sent `{}` to topic `{}` in partition `{}`, offset `{}`.";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    private final CreateMessageHelper messageHelper;

    private final KafkaOutboundTopicProperties kafkaOutboundTopicProperties;

    private final KafkaDebugProperties debugProperties;
    
    @PostConstruct
    private void initialize() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void sendShipmentToQShip(Shipment shipment) {
        try {
            sendQshipSegment(shipment);
        } catch (final Exception e) {
            log.error(ERROR_IN_MAPPING_SENDING_MESSAGE + e.getMessage(), e);
        }
    }

    public void sendShipmentWithJourneyToQShip(Shipment shipment, ShipmentJourney journey) {
        try {
            sendQshipSegment(shipment, journey);
        } catch (final Exception e) {
            log.error(ERROR_IN_MAPPING_SENDING_MESSAGE + e.getMessage(), e);
        }
    }

    public void sendSegmentDispatch(List<Shipment> shipments, ShipmentJourney journey, SegmentDispatchType type, DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        try {
            sendSegmentsDispatch(shipments, journey, type, dspSegmentMsgUpdateSource);
        } catch (final Exception e) {
            log.error(ERROR_IN_MAPPING_SENDING_MESSAGE + e.getMessage(), e);
        }
    }

    public void sendMilestoneMessage(Shipment shipment, MilestoneMessage milestone, TriggeredFrom from) {
        if (isNull(milestone)) {
            milestone = messageHelper.createMilestoneMessage(shipment);
            milestone.setTriggeredFrom(from);
        }
        final String msg = serializeObjectToJsonText(milestone);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getShipmentMilestoneTopic(), msg);
    }

    public void sendMilestoneMessage(MilestoneMessage milestone) {
        if (isNull(milestone)) {
            return;
        }
        final String message = serializeObjectToJsonText(milestone);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getShipmentMilestoneTopic(), message);
    }

    public void sendMilestoneError(final MilestoneError milestoneError) {
        final String msg = serializeObjectToJsonText(milestoneError);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getDispatchMilestoneErrorTopic(), msg);
    }

    public void sendShipmentPath(final Shipment shipment) {
        final String msg = serializeObjectToJsonText(messageHelper.createShipShipmentPathMessage(shipment));
        kafkaTemplate.send(kafkaOutboundTopicProperties.getShipmentPathTopic(), msg);
    }

    public String generateSegmentsDispatchMessage(final List<Shipment> shipmentList,
                                                  final ShipmentJourney journey,
                                                  final SegmentDispatchType type,
                                                  final DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        return serializeObjectToJsonText(messageHelper.createSegmentsDispatchMessage(shipmentList, journey, type, dspSegmentMsgUpdateSource));
    }

    public String generateSegmentsDispatchMessage(final List<Shipment> shipmentList,
                                                  final PackageJourneySegment updatedSegment,
                                                  final SegmentDispatchType type,
                                                  final DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        return serializeObjectToJsonText(messageHelper.createSegmentsDispatchMessage(shipmentList, updatedSegment, type, dspSegmentMsgUpdateSource));
    }

    public void sendSegmentsDispatch(final Shipment shipment,
                                     final SegmentDispatchType type,
                                     final DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        final String segmentDispatchMessage = generateSegmentsDispatchMessage(List.of(shipment), shipment.getShipmentJourney(), type, dspSegmentMsgUpdateSource);
        sendMessageToSegmentDispatchTopic(shipment, segmentDispatchMessage);
    }

    public void sendSegmentsDispatch(final Shipment shipment,
                                     final PackageJourneySegment updatedSegment,
                                     final SegmentDispatchType type,
                                     final DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        final String segmentDispatchMessage = generateSegmentsDispatchMessage(List.of(shipment), updatedSegment, type, dspSegmentMsgUpdateSource);
        sendMessageToSegmentDispatchTopic(shipment, segmentDispatchMessage);
    }

    public void sendSegmentsDispatch(final List<Shipment> shipmentList, final ShipmentJourney journey,
                                     final SegmentDispatchType type, final DspSegmentMsgUpdateSource dspSegmentMsgUpdateSource) {
        final String segmentDispatchMessage = generateSegmentsDispatchMessage(shipmentList, journey, type, dspSegmentMsgUpdateSource);
        sendMessageToSegmentDispatchTopic(shipmentList.get(0), segmentDispatchMessage);
    }

    private void sendMessageToSegmentDispatchTopic(Shipment shipment, String msg) {
        String orderId = Optional.ofNullable(shipment.getOrder()).map(Order::getId).orElse(shipment.getOrderId());
        sendMsgToKafka(randomUUID().toString(), kafkaOutboundTopicProperties.getSegmentsDispatchTopic(), msg, orderId);
    }

    public List<String> getQshipSegment(final Shipment shipment) {
        final List<QshipSegmentMessage> msgList = messageHelper.createQshipSegmentMessageList(shipment);

        return msgList.stream().map(this::serializeObjectToJsonText).toList();
    }

    public List<String> getQshipSegment(final Shipment shipment, ShipmentJourney journey) {
        final List<QshipSegmentMessage> msgList = messageHelper.createQshipSegmentMessageList(shipment, journey);

        return msgList.stream().map(this::serializeObjectToJsonText).toList();
    }

    public void sendQshipSegment(final Shipment shipment) {
        getQshipSegment(shipment)
                .forEach(msgStr -> {
                    log.debug(DEBUG_SENDING_MESSAGE_QSHIP_SEGMENT_ID, msgStr);
                    sendMsgToKafka(randomUUID().toString(), kafkaOutboundTopicProperties.getQShipSegmentTopic(), msgStr, shipment.getOrderId());
                });
    }

    public void sendQshipSegment(final Shipment shipment, final ShipmentJourney journey) {
        getQshipSegment(shipment, journey)
                .forEach(msgStr -> {
                    log.debug(DEBUG_SENDING_MESSAGE_QSHIP_SEGMENT_ID, msgStr);
                    sendMsgToKafka(randomUUID().toString(), kafkaOutboundTopicProperties.getQShipSegmentTopic(), msgStr, shipment.getOrderId());
                });
    }

    public void sendPackageDimensions(final Shipment shipment) {

        final String msg = serializeObjectToJsonText(messageHelper.createShipmentPackageDimensions(shipment));
        kafkaTemplate.send(kafkaOutboundTopicProperties.getShipmentPackageDimensionsTopic(), msg);
    }

    public void sendUpdatedSegmentFromShipment(final ShipmentMessageDto shipmentMessageDto, final PackageJourneySegment packageJourneySegment) {
        QshipSegmentMessage qshipSegmentMessage = messageHelper.createQshipSegmentMessage(shipmentMessageDto, packageJourneySegment);

        final String msg = serializeObjectToJsonText(qshipSegmentMessage);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getQShipSegmentTopic(), packageJourneySegment.getSegmentId(), msg);
    }

    public void sendUpdatedSegmentFromShipment(final Shipment shipment, final PackageJourneySegment segment) {
        QshipSegmentMessage qshipSegmentMessage = messageHelper.createQshipSegmentMessage(shipment, segment);

        final String msg = serializeObjectToJsonText(qshipSegmentMessage);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getQShipSegmentTopic(), msg);
    }

    public void sendUpdatedSegmentFromShipment(final Shipment shipment, String updatedSegmentId) {
        List<QshipSegmentMessage> qshipSegmentMessageList = messageHelper.createQshipSegmentMessageList(shipment);
        QshipSegmentMessage updatedQshipSegmentMessage = qshipSegmentMessageList.stream()
                .filter(qshipSegmentMessage -> qshipSegmentMessage.getId().equals(updatedSegmentId))
                .findFirst().orElse(null);

        if (updatedQshipSegmentMessage == null) {
            log.debug("Segment id: {}  was found for shipmentId: {} ", updatedSegmentId, shipment.getId());
            return;
        }
        final String msg = serializeObjectToJsonText(updatedQshipSegmentMessage);
        kafkaTemplate.send(kafkaOutboundTopicProperties.getQShipSegmentTopic(), msg);
    }

    public void sendShipmentCancelMessage(final Shipment shipment) {
        final String msg = serializeObjectToJsonText(messageHelper.createShipmentCancelMessage(shipment));
        kafkaTemplate.send(kafkaOutboundTopicProperties.getShipmentCancelTopic(), msg);
    }

    public void subscribeFlight(FlightStatsRequest flight) {
        try {
            final String uuid = randomUUID().toString();
            final String msg = getFlightSubscriptionMessage(flight, uuid);
            String flightKey = flight.getFlightNumber() + flight.getCarrier() + flight.getDepartureDate() + flight.getOrigin() + flight.getDestination();
            sendMsgToKafka(uuid, kafkaOutboundTopicProperties.getFlightStatsOutboundTopic(), msg, flightKey);
        } catch (final Exception e) {
            log.error(ERROR_IN_MAPPING_SENDING_MESSAGE + e.getMessage(), e);
        }
    }

    public String getFlightSubscriptionMessage(final FlightStatsRequest flight, final String uuid) {
        return serializeObjectToJsonText(messageHelper.createFlightSubscriptionMessage(flight, uuid));
    }

    public void sendDispatchCanceled(Shipment shipment, String segmentId, String reason) {
        final String msg = serializeObjectToJsonText(messageHelper.createSegmentCancelMessage(shipment, segmentId, reason));
        kafkaTemplate.send(kafkaOutboundTopicProperties.getSegmentCancelTopic(), msg);
    }

    private void sendMsgToKafka(String uuidHeader, String topic, String message, String key) {
        if (isNull(message)) {
            return;
        }
        ProducerRecord<String, String> producerRecord = buildProducerRecord(uuidHeader, topic, message, key);
        if (debugProperties.isWriteResultEnabled()) {
            handleKafkaSendWithDebug(producerRecord, uuidHeader, topic);
        } else {
            kafkaTemplate.send(producerRecord);
        }
        log.info(INFO_MESSAGE_WITH_UUID_HEADER_TO_TOPIC_SENT, uuidHeader, producerRecord.key(), topic);
    }

    private ProducerRecord<String, String> buildProducerRecord(String uuidHeader, String topic, String message, String key) {
        List<Header> headers = List.of(new RecordHeader(MDC_UUID, uuidHeader.getBytes()));
        String producerKey = Optional.ofNullable(key).orElse(DEFAULT_MDC_KEY + "-" + UUID.randomUUID());
        return new ProducerRecord<>(topic, null, producerKey, message, headers);
    }

    private void handleKafkaSendWithDebug(ProducerRecord<String, String> producerRecord, String uuidHeader, String topic) {
        ListenableFuture<SendResult<String, String>> kafkaFuture = kafkaTemplate.send(producerRecord);
        handleKafkaResult(kafkaFuture, uuidHeader, topic);
    }

    private void handleKafkaResult(ListenableFuture<SendResult<String, String>> future, String uuidHeader, String topic) {
        if (debugProperties.isWriteResultEnabled()) {
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.error(ERROR_MESSAGE_ON_KAFKA, uuidHeader, topic, ex.getMessage());
                }

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    RecordMetadata metadata = result.getRecordMetadata();
                    int partition = metadata.partition();
                    long offset = metadata.offset();
                    log.debug(DEBUG_MESSAGE_SENT_TO_KAFKA, uuidHeader, topic, partition, offset);
                }
            });
        }
    }

    private <T> String serializeObjectToJsonText(T object) {
        String msg = null;
        try {
            msg = mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error(ERROR_OBJECT_KAFKA_SERIALIZATION, object, e);
        }
        return msg;
    }
}
