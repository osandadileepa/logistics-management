package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.domain.Cod;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.validator.constraint.FieldType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.List;

@Data
public class MilestoneUpdateRequest {
    @FieldType(type = String.class)
    @JsonAlias("order_no")
    private String orderNumber;

    @NotBlank
    @FieldType(type = String.class)
    private String segmentId;

    @NotBlank
    @FieldType(type = String.class)
    private String vendorId;

    @NotBlank
    @FieldType(type = String.class)
    private String milestone;

    @NotBlank
    @FieldType(type = Temporal.class)
    @JsonAlias({"milestone_date_and_time", "milestone_timestamp"})
    private String milestoneTime;

    @JsonAlias("awb_number")
    @FieldType(type = String.class)
    private String waybillNumber;

    @FieldType(type = String.class)
    private String recipientName;

    @JsonAlias("dept_floor_suite_comments")
    @FieldType(type = String.class)
    private String departmentFloorSuiteComments;

    @JsonAlias("pod_date_and_time")
    @FieldType(type = Temporal.class)
    private String proofOfDeliveryTime;

    @FieldType(type = String.class)
    private String branchName;

    @NotNull
    @FieldType(type = MilestoneSource.class)
    private MilestoneSource source;

    @FieldType(type = String.class)
    private String senderName;

    @FieldType(type = OffsetDateTime.class)
    private OffsetDateTime eta;

    @FieldType(type = String.class)
    private String remarks;

    @FieldType(type = Coordinate.class)
    private Coordinate milestoneCoordinates;

    @NotEmpty
    @FieldType(type = List.class)
    private List<String> shipmentIds;

    @FieldType(type = String.class)
    private String failedReason;

    @FieldType(type = String.class)
    private String failedReasonCode;

    @FieldType(type = Cod.class)
    private Cod cod;

    @FieldType(type = List.class)
    private List<HostedFile> attachments;
}
