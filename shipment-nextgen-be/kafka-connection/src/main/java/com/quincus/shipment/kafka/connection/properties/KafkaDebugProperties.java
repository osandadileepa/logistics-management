package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.debug")
@Data
@Component
public class KafkaDebugProperties extends KafkaProperties {
    private boolean writeResultEnabled;
}
