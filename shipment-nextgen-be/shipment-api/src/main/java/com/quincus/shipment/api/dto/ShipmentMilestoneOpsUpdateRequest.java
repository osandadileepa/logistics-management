package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.validator.constraint.FieldType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.temporal.Temporal;
import java.util.List;

@Data
@NoArgsConstructor
public class ShipmentMilestoneOpsUpdateRequest {
    @NotEmpty
    private String milestoneName;
    @NotEmpty
    private String milestoneCode;
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String shipmentTrackingId;
    @Size(max = 2000, message = "Must be maximum of 2000 characters.")
    private String notes;
    @NotBlank
    @FieldType(type = Temporal.class)
    private String milestoneTime;
    @Size(max = 10, message = "Must be maximum of 10 attachments.")
    private List<HostedFile> attachments;
    @NotNull
    private UserLocation usersLocation;
}
