package com.quincus.shipment.kafka.connection.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.consumer-groups.orders")
public class KafkaOrdersConsumerProperties extends KafkaConsumerProperties {
}
