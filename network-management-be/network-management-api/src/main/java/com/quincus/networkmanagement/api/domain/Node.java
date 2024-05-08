package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.api.validator.constraint.ValidNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Setter
@ValidNode
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Node extends Tenant {
    private String id;
    @NotNull
    private NodeType nodeType;
    @NotBlank
    private String nodeCode;
    private String description;
    private Boolean active;
    private List<String> tags;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    @NotNull
    @Valid
    private Facility facility;
    @Valid
    private Partner vendor;
    @Valid
    @NotNull
    private CapacityProfile capacityProfile;
    @Valid
    @NotNull
    private MeasurementUnits measurementUnits;
    @NotNull
    @Valid
    private OperatingHours operatingHours;
    @NotNull
    @Valid
    private ShipmentProfile shipmentProfile;
    private String timezone;
    @JsonIgnore
    private boolean deleted;

    public Node(Facility facility) {
        this.facility = facility;
    }
}
