package com.quincus.shipment.kafka.consumers.utility;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static java.util.UUID.randomUUID;

@Slf4j
@UtilityClass
public class KafkaConsumerUtils {

    private static final String MDC_UUID = "UUID";
    private static final String LOG_MDC_UUID = "UUID `{}`";

    public static String generateUUIDForConsumerRecord(final ConsumerRecord<String, String> consumerRecord) {
        Iterator<Header> it = consumerRecord.headers().headers(MDC_UUID).iterator();
        String uuid = randomUUID().toString();
        if (it.hasNext()) {
            uuid = new String(it.next().value(), StandardCharsets.UTF_8);
        }

        logUUID(uuid);
        return uuid;
    }

    public static void logConsumerRecordDetails(String topicDescription, ConsumerRecord<String, String> consumerRecord, String transactionId) {
        log.info("Received message from `{}` with Topic: `{}`, Partition: `{}`, Offset: `{}`, Key: `{}` with Transaction id: `{}`",
                topicDescription,
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.key(),
                transactionId);
    }

    private static void logUUID(final String uuid) {
        MDC.put(MDC_UUID, uuid);
        log.info(LOG_MDC_UUID, uuid);
    }
}
