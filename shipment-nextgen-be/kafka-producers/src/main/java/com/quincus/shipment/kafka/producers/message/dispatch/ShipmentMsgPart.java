package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.HostedFile;
import lombok.Data;

import java.util.List;

@Data
public class ShipmentMsgPart {
    private String shipmentId;
    private String shipmentTrackingId;
    private List<String> shipmentReferenceIds;
    private List<HostedFile> shipmentAttachments;
    private List<String> shipmentTags;
    @JsonProperty("package")
    PackageMsgPart packageVal;
}
