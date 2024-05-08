package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.topics.dead-letter")
@Data
@Component
public class KafkaDeadLetterProperties extends KafkaProperties {

    private String dispatchModuleMilestoneDlq;
    private String flightStatsInboundDlq;
    private String orderModuleDlq;
    private String qportalLocationDlq;

    public String getDispatchModuleMilestoneDlqTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getDispatchModuleMilestoneDlq());
    }

    public String getFlightStatsInboundDlqTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getFlightStatsInboundDlq());
    }

    public String getOrderModuleDlqTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getOrderModuleDlq());
    }

    public String getQportalLocationDlqTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getQportalLocationDlq());
    }
}
