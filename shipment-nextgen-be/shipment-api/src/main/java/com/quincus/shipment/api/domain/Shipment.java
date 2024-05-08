package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Shipment {
    public static final String ORIGIN_PROPERTY_NAME = "origin";
    public static final String DESTINATION_PROPERTY_NAME = "destination";

    @UUID(required = false)
    private String id;
    @NotBlank
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String shipmentTrackingId;
    @NotNull
    @Valid
    private Order order;
    @NotNull
    @Valid
    private Sender sender;
    @NotNull
    @Valid
    private Consignee consignee;
    @NotBlank
    @Size(max = 256, message = "Maximum of 256 characters allowed.")
    private String pickUpLocation;
    @NotBlank
    @Size(max = 256, message = "Maximum of 256 characters allowed.")
    private String deliveryLocation;
    @NotNull
    @Valid
    private ServiceType serviceType;
    @NotBlank
    @UUID(required = false)
    private String userId;
    private String userLocationId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Organization organization;
    @NotNull
    @Valid
    private Package shipmentPackage;
    private Milestone milestone;
    @Valid
    private Customer customer;
    @Valid
    @Size(max = 20)
    private List<Instruction> instructions;
    @Size(max = 2000, message = "Maximum of 2000 characters allowed.")
    private String notes;
    @Size(max = 256, message = "Maximum of 256 characters allowed.")
    private String returnLocation;
    @Size(max = 10)
    private List<String> extraCareInfo;
    @Size(max = 10)
    private List<String> insuranceInfo;
    @Valid
    private ShipmentJourney shipmentJourney;
    @Valid
    @Size(max = 20)
    private List<Milestone> milestoneEvents;
    @Valid
    private Address origin;
    @Valid
    private Address destination;
    private ShipmentStatus status;
    private EtaStatus etaStatus;
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String partnerId;
    @Size(max = 5)
    private List<String> shipmentReferenceId;
    private List<String> shipmentTags;
    @Size(max = 10)
    @Valid
    private List<HostedFile> shipmentAttachments;
    @Size(max = 64, message = "Maximum of 64 characters allowed.")
    private String internalOrderId;
    @Size(max = 64, message = "Maximum of 64 characters allowed.")
    private String externalOrderId;
    @Size(max = 64, message = "Maximum of 64 characters allowed.")
    private String customerOrderId;
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String orderId;
    private LocalDateTime lastUpdatedTime;
    private LocalDateTime createdTime;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean updated;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean deleted;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean segmentUpdated;
    private boolean segmentsUpdatedFromSource;
    @Size(max = 255, message = "Maximum of 255 characters allowed.")
    private String description;
    private UnitOfMeasure distanceUom;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
