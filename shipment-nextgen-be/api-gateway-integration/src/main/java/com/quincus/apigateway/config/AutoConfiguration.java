package com.quincus.apigateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "api-gateway.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.apigateway")
public class AutoConfiguration {
}
