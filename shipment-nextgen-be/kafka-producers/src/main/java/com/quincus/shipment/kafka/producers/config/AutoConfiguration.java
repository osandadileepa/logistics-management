package com.quincus.shipment.kafka.producers.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", prefix = "feature")
@EnableAsync
@Configuration
@ComponentScan("com.quincus.shipment.kafka.producers")
public class AutoConfiguration {
    
}
