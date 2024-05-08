package com.quincus.s3.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "s3-integration.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.s3")
public class AutoConfiguration {
}
