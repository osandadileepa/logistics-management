package com.quincus.qportal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "qportal.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.qportal")
public class AutoConfiguration {


}