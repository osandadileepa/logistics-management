package com.quincus.shipment.kafka.producers.message.dispatch;

import lombok.Data;

@Data
public class SegmentCancelMessage {
    private String shipmentId;
    private String organisationId;
    private String segmentId;
    private String orderId;
    private String reason;
}
