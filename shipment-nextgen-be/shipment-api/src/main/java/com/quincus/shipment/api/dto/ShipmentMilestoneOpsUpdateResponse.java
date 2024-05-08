package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.deserializer.OffsetDateTimeDeserializer;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.serializer.OffsetDateTimeSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class ShipmentMilestoneOpsUpdateResponse {
    private String shipmentId;
    private String shipmentTrackingId;
    private String previousMilestoneName;
    private String currentMilestoneName;
    private String currentMilestoneId;
    private String previousMilestoneCode;
    private String currentMilestoneCode;
    private String notes;
    private String organizationId;
    private String updatedBy;
    private UserLocation usersLocation;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime milestoneTime;
    private List<HostedFile> attachments;
}