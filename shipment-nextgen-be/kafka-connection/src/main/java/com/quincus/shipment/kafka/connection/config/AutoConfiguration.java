package com.quincus.shipment.kafka.connection.config;

import com.quincus.ext.YamlPropertySourceFactory;
import com.quincus.shipment.kafka.connection.properties.KafkaConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaDeadLetterProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaDebugProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaDispatchMilestoneConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaFlightStatsConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaInboundTopicProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaLocationsConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOrdersConsumerProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOutboundTopicProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", prefix = "feature")
@Configuration
@PropertySource(
        value = {
                "classpath:config/kafka-connection.yml",
                "classpath:config/kafka-connection-${spring.profiles.active}.yml"},
        factory = YamlPropertySourceFactory.class)
@ComponentScan("com.quincus.shipment.kafka.connection")
@EnableConfigurationProperties({
        KafkaConsumerProperties.class,
        KafkaInboundTopicProperties.class,
        KafkaOutboundTopicProperties.class,
        KafkaDeadLetterProperties.class,
        KafkaDispatchMilestoneConsumerProperties.class,
        KafkaFlightStatsConsumerProperties.class,
        KafkaOrdersConsumerProperties.class,
        KafkaLocationsConsumerProperties.class,
        KafkaDebugProperties.class,
})
public class AutoConfiguration {

}
