package com.quincus.mme.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan("com.quincus.mme")
@ConditionalOnProperty(name = "mme.enabled", havingValue = "true", prefix = "feature")
public class AutoConfiguration {
}
