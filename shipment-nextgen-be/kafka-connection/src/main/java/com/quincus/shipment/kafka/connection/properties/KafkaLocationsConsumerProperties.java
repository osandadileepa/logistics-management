package com.quincus.shipment.kafka.connection.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.consumer-groups.locations")
public class KafkaLocationsConsumerProperties extends KafkaConsumerProperties {
}
