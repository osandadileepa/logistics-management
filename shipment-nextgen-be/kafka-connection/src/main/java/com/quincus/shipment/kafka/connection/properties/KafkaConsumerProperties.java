package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Data
public class KafkaConsumerProperties extends KafkaProperties {
    public static final String CONSUMER_ID_PATTERN = "{0}{1}{2}";
    private String id;
    private Integer sessionTimeout;
    private Integer heartbeatInterval;
    private Integer concurrency;
    private boolean enableAutoCommit;
    private String acknowledgementMode;
    private Integer maxPollIntervalMs;
    private boolean enable = true;
    private boolean kafkaPreAuthenticationRequired = true;
    private boolean kafkaPreAuthenticationWithUserId = false;

    public String getConsumerGroupId() {
        return MessageFormat.format(CONSUMER_ID_PATTERN, getEnvironment(), getModule(), getId());
    }
}

