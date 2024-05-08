package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.ISODateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Order {
    @NotBlank
    private String id;
    @NotBlank
    @Size(max = 64, message = "Maximum of 64 characters allowed.")
    private String orderIdLabel;
    @Size(max = 255, message = "Maximum of 255 characters allowed.")
    private String trackingUrl;
    @NotBlank
    @Size(max = 50, message = "Maximum of 50 characters allowed.")
    private String status;
    private String timeCreated;
    @Size(max = 2000, message = "Must be maximum of 2000 characters.")
    private String notes;
    private List<String> customerReferenceId;
    @Size(max = 32, message = "Maximum of 32 characters allowed.")
    private String group;
    @NotNull
    @ISODateTime
    @Size(max = 50, message = "Maximum of 50 characters allowed.")
    private String pickupStartTime;
    @NotNull
    @ISODateTime
    @Size(max = 50, message = "Maximum of 50 characters allowed.")
    private String pickupCommitTime;
    @NotNull
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String pickupTimezone;
    @NotNull
    @ISODateTime
    @Size(max = 50, message = "Maximum of 50 characters allowed.")
    private String deliveryStartTime;
    @NotNull
    @ISODateTime
    @Size(max = 50, message = "Maximum of 50 characters allowed.")
    private String deliveryCommitTime;
    @NotNull
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String deliveryTimezone;
    private String data;
    private List<String> tags;
    @Size(max = 32, message = "Maximum of 32 characters allowed.")
    private String opsType;
    @Valid
    private List<OrderAttachment> attachments;
    private boolean segmentsUpdated;
    @Size(max = 255, message = "Maximum of 255 characters allowed.")
    private String cancelReason;
    private boolean usedOpenApi;
    private String code;
    private List<Instruction> instructions;
    private String organizationId;
    private List<OrderReference> orderReferences;
}
