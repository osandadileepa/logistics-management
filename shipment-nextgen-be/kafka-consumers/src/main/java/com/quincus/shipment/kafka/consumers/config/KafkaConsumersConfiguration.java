package com.quincus.shipment.kafka.consumers.config;

import com.quincus.shipment.kafka.consumers.handler.KafkaErrorHandler;
import com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthenticationRecordInterceptor;
import com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthenticationWithUserDetailRecordInterceptor;
import com.quincus.shipment.kafka.connection.properties.KafkaConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaDispatchMilestoneConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaFlightStatsConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaLocationsConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOrdersConsumerProperties;
import com.quincus.web.security.AuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.RecordInterceptor;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumersConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final KafkaErrorHandler kafkaErrorHandler;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> ordersKafkaListenerContainerFactory(
            KafkaOrdersConsumerProperties kafkaOrdersConsumerProperties) {
        return createFactory(kafkaOrdersConsumerProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> dispatchMilestoneKafkaListenerContainerFactory(
            KafkaDispatchMilestoneConsumerProperties kafkaDispatchMilestoneConsumerProperties) {
        return createFactory(kafkaDispatchMilestoneConsumerProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> flightStatsKafkaListenerContainerFactory(
            KafkaFlightStatsConsumerProperties kafkaFlightStatsConsumerProperties) {
        return createFactory(kafkaFlightStatsConsumerProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> locationsKafkaListenerContainerFactory(
            KafkaLocationsConsumerProperties kafkaLocationsConsumerProperties) {
        return createFactory(kafkaLocationsConsumerProperties);
    }
    
    private RecordInterceptor<String, String> getPreAuthenticationInterceptor(KafkaConsumerProperties kafkaConsumerProperties) {
        if (kafkaConsumerProperties.isKafkaPreAuthenticationWithUserId()) {
            return new KafkaPreAuthenticationWithUserDetailRecordInterceptor(authenticationProvider);
        }
        return new KafkaPreAuthenticationRecordInterceptor(authenticationProvider);
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> createFactory(
            KafkaConsumerProperties kafkaConsumerProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(kafkaConsumerProperties));
        factory.setConcurrency(kafkaConsumerProperties.getConcurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.valueOf(kafkaConsumerProperties.getAcknowledgementMode()));
        factory.setCommonErrorHandler(kafkaErrorHandler);
        if (kafkaConsumerProperties.isKafkaPreAuthenticationRequired()) {
            factory.setRecordInterceptor(getPreAuthenticationInterceptor(kafkaConsumerProperties));
        }
        return factory;
    }

    private ConsumerFactory<String, String> consumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getUrl());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getConsumerGroupId());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeout());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getHeartbeatInterval());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getMaxPollIntervalMs());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
