package com.quincus.shipment.kafka.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "kafka.admin.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.shipment.kafka.admin")
public class AutoConfiguration {

}
