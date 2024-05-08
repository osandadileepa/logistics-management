package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.quincus.shipment.api.constant.ResponseCode;
import lombok.Data;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@JsonPropertyOrder({"orderNumber", "segmentId", "milestone", "responseCode", "responseMessage", "timestamp"})
public class MilestoneUpdateResponse {
    @JsonProperty("order_no")
    private String orderNumber;
    @JsonProperty("segment_id")
    private String segmentId;
    private String milestone;
    @JsonProperty("vendor_id")
    private String vendorId;
    @JsonProperty("shipment_ids")
    private List<String> shipmentIds;
    @JsonProperty("response_code")
    private ResponseCode responseCode;
    @JsonProperty("response_message")
    private String responseMessage;
    private Instant timestamp;

    public String getTimestamp() {
        if (timestamp == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return formatter.format(timestamp);
    }
}
