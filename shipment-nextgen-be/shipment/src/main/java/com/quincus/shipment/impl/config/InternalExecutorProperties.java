package com.quincus.shipment.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shipment.async.internal-task-executor")
public class InternalExecutorProperties extends AsyncProperties {
}
