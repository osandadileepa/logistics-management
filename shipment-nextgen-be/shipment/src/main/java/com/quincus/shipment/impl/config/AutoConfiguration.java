package com.quincus.shipment.impl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "shipment.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.shipment.impl")
public class AutoConfiguration {

}
