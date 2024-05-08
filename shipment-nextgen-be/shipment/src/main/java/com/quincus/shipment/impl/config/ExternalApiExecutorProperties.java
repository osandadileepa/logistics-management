package com.quincus.shipment.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shipment.async.external-task-executor")
public class ExternalApiExecutorProperties extends AsyncProperties {
}
