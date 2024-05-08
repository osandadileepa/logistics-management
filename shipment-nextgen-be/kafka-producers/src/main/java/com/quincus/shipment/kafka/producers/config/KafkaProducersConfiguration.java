package com.quincus.shipment.kafka.producers.config;

import com.quincus.shipment.kafka.connection.properties.KafkaDeadLetterProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOutboundTopicProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaProducersConfiguration {

    @Bean
    @Autowired
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate(@Qualifier("kafkaOutboundTopicProperties")
                                                       KafkaOutboundTopicProperties kafkaProperties) {
        return createKafkaTemplate(kafkaProperties.getUrl());
    }

    @Bean
    @Autowired
    public KafkaTemplate<String, String> dlqKafkaTemplate(@Qualifier("kafkaDeadLetterProperties")
                                                          KafkaDeadLetterProperties kafkaDeadLetterProperties) {
        return createKafkaTemplate(kafkaDeadLetterProperties.getUrl());
    }

    private KafkaTemplate<String, String> createKafkaTemplate(String url) {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(configProps);
        return new KafkaTemplate<>(factory);
    }

}
