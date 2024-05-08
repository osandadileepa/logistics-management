package com.quincus.shipment.kafka.consumers.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.shipment.kafka.consumers")
public class AutoConfiguration {

}
