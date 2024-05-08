package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.topics.inbound")
@Data
@Component
public class KafkaInboundTopicProperties extends KafkaProperties {

    private String dispatchModuleMilestone;
    private String flightStatsInbound;
    private String orderModule;
    private String qportalLocation;

    public String getDispatchModuleMilestoneTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getDispatchModuleMilestone());
    }

    public String getFlightStatsInboundTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getFlightStatsInbound());
    }

    public String getOrderModuleTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getOrderModule());
    }

    public String getQportalLocationTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getQportalLocation());
    }
}
