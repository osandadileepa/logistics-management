package com.quincus.networkmanagement.impl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ConditionalOnProperty(name = "network-management.enabled", havingValue = "true", prefix = "feature")
@Configuration
@ComponentScan("com.quincus.networkmanagement.impl")
@EnableAspectJAutoProxy
public class AutoConfiguration {

}
