package com.quincus.shipment.kafka.producers.message.qship;

import lombok.Data;

@Data
public class PackageMsgPart {
    private String id;
    private String refId;
    private String additionalData1;
}
