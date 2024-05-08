package com.quincus.shipment.kafka.admin.web;

import com.quincus.shipment.kafka.admin.ProducersController;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController

@AllArgsConstructor
public class ProducersControllerImpl implements ProducersController {

    public static final String MDC_UUID = "UUID";
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public String send(final String name, final String message) {
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(name, message);
        final String uuid = MDC.get(MDC_UUID);
        producerRecord.headers().add(MDC_UUID, uuid.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(producerRecord);
        return uuid;
    }

}