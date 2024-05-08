package com.quincus.shipment.kafka.consumers.interceptor;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.CLEARING_THE_CONTEXT;

@AllArgsConstructor
@Slf4j
public abstract class AbstractKafkaPreAuthenticationRecordInterceptor implements RecordInterceptor<String, String> {

    @Override
    public ConsumerRecord<String, String> intercept(@NonNull ConsumerRecord<String, String> consumerRecord, @NonNull Consumer<String, String> consumer) {
        return RecordInterceptor.super.intercept(consumerRecord, consumer);
    }

    @Override
    public void success(@NonNull ConsumerRecord<String, String> consumerRecord, @NonNull Consumer<String, String> consumerParam) {
        RecordInterceptor.super.success(consumerRecord, consumerParam);
    }

    @Override
    public void failure(@NonNull ConsumerRecord<String, String> consumerRecord, @NonNull Exception consumerException, @NonNull Consumer<String, String> consumerParam) {
        RecordInterceptor.super.failure(consumerRecord, consumerException, consumerParam);
    }

    @Override
    public void afterRecord(@NonNull ConsumerRecord<String, String> consumerRecord, @NonNull Consumer<String, String> consumerParam) {
        log.info(CLEARING_THE_CONTEXT);
        SecurityContextHolder.clearContext();
        RecordInterceptor.super.afterRecord(consumerRecord, consumerParam);
    }
}
