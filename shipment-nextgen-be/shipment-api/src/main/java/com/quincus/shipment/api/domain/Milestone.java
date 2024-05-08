package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.deserializer.OffsetDateTimeDeserializer;
import com.quincus.shipment.api.serializer.InstantSerializer;
import com.quincus.shipment.api.serializer.OffsetDateTimeSerializer;
import com.quincus.shipment.api.validator.constraint.ValidMilestone;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ValidMilestone
public class Milestone implements Comparable<Milestone> {
    @UUID(required = false)
    private String id;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createTime;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant modifyTime;
    @Size(max = 48)
    private String milestoneRefId;
    private MilestoneCode milestoneCode;
    @Size(max = 48)
    private String milestoneName;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime milestoneTime;
    private String milestoneTimezone;
    @UUID(required = false)
    private String organizationId;
    @Size(max = 48)
    private String serviceType;
    @Size(max = 48)
    private String jobType;
    @UUID(required = false)
    private String fromLocationId;
    @UUID(required = false)
    private String fromCountryId;
    @UUID(required = false)
    private String fromStateId;
    @UUID(required = false)
    private String fromCityId;
    @UUID(required = false)
    private String fromWardId;
    @UUID(required = false)
    private String fromDistrictId;
    private Coordinate fromCoordinates;
    @UUID(required = false)
    private String toLocationId;
    @UUID(required = false)
    private String toCountryId;
    @UUID(required = false)
    private String toStateId;
    @UUID(required = false)
    private String toCityId;
    @UUID(required = false)
    private String toWardId;
    @UUID(required = false)
    private String toDistrictId;
    private Coordinate toCoordinates;
    @UUID(required = false)
    private String userId;
    @UUID(required = false)
    private String partnerId;
    @UUID(required = false)
    private String shipmentId;
    @Size(max = 48)
    private String shipmentTrackingId;
    @UUID(required = false)
    private String segmentId;
    @Size(max = 255)
    private String userName;
    @UUID(required = false)
    private String hubId;
    @UUID(required = false)
    private String hubCityId;
    @UUID(required = false)
    private String hubStateId;
    @UUID(required = false)
    private String hubCountryId;
    private String hubTimeZone;
    @UUID(required = false)
    private String driverId;
    @Size(max = 64)
    private String driverName;
    @Size(max = 10)
    private String driverPhoneCode;
    @Size(max = 24)
    private String driverPhoneNumber;
    @Size(max = 128)
    private String driverEmail;
    @Size(max = 48)
    private String vehicleId;
    @Size(max = 64)
    private String vehicleType;
    @Size(max = 64)
    private String vehicleName;
    @Size(max = 24)
    private String vehicleNumber;
    @Size(max = 128)
    private String senderName;
    @Size(max = 128)
    private String senderCompany;
    @Size(max = 128)
    private String senderDepartment;
    @Size(max = 128)
    private String receiverName;
    @Size(max = 128)
    private String receiverCompany;
    @Size(max = 128)
    private String receiverDepartment;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime eta;
    private String etaTimezone;
    @Size(max = 255)
    private String failedReason;
    @Size(max = 32)
    private String failedReasonCode;
    private Coordinate milestoneCoordinates;
    private MilestoneAdditionalInfo additionalInfo;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JsonNode data;
    @Size(max = 64)
    private String orderNumber;
    @UUID(required = false)
    private String vendorId;
    @Size(max = 50)
    private String waybillNumber;
    @Size(max = 2000)
    private String departmentFloorSuiteComments;
    @Size(max = 120)
    private String branchName;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime proofOfDeliveryTime;
    private String proofOfDeliveryTimezone;
    @Size(max = 50)
    private String externalSegmentId;
    @Size(max = 64)
    private String externalOrderId;
    @JsonIgnore
    private boolean segmentUpdatedFromMilestone;
    private MilestoneSource source;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milestone that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean isFailureClassification() {
        return this.getMilestoneCode() == MilestoneCode.DSP_PICKUP_FAILED || this.getMilestoneCode() == MilestoneCode.DSP_DELIVERY_FAILED;
    }

    @Override
    public int compareTo(Milestone m) {
        if (m == null || m.getMilestoneTime() == null) return 0;
        return getMilestoneTime().compareTo(m.getMilestoneTime());
    }
}
