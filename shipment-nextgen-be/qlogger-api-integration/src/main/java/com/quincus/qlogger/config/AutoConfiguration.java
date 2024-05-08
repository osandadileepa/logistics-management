package com.quincus.qlogger.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "qlogger.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.qlogger")
public class AutoConfiguration {


}