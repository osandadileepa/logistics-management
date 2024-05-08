package com.quincus.shipment.kafka.connection.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.topics.outbound")
@Data
@Component
public class KafkaOutboundTopicProperties extends KafkaProperties {
    private String shipmentPath;
    private String shipmentSegment;
    private String shipmentMilestone;
    private String segmentDispatch;
    private String segmentDispatchChange;
    private String segmentCancel;
    private String qShipSegment;
    private String shipmentPackageDimensions;
    private String shipmentCancel;
    private String dispatchMilestoneError;
    private String flightStatsOutbound;

    public String getShipmentPathTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getShipmentPath());
    }

    public String getShipmentMilestoneTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getShipmentMilestone());
    }

    public String getSegmentsDispatchTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getSegmentDispatch());
    }

    public String getQShipSegmentTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getQShipSegment());
    }

    public String getShipmentPackageDimensionsTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getShipmentPackageDimensions());
    }

    public String getShipmentCancelTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getShipmentCancel());
    }

    public String getDispatchMilestoneErrorTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getDispatchMilestoneError());
    }

    public String getFlightStatsOutboundTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getFlightStatsOutbound());
    }

    public String getSegmentCancelTopic() {
        return MessageFormat.format(PATTERN, getEnvironment(), getSegmentCancel());
    }
}
