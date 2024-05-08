package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.deserializer.MoneyDeserializer;
import com.quincus.networkmanagement.api.validator.constraint.ValidConnection;
import com.quincus.networkmanagement.api.validator.constraint.ValidRRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ValidConnection
public class Connection extends Tenant {
    private String id;
    @NotBlank
    private String connectionCode;
    @NotNull
    @Valid
    private Partner vendor;
    private List<String> tags;
    private Boolean active;
    @NotNull
    private TransportType transportType;
    private VehicleType vehicleType;
    @NotNull
    private Node departureNode;
    @NotNull
    private Node arrivalNode;
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal cost;
    @NotNull
    @Valid
    private Currency currency;
    @NotNull
    @Valid
    private ShipmentProfileExtension shipmentProfile;
    @Min(0)
    @Max(999999)
    @NotNull
    private Integer duration;
    @Min(0)
    @Max(999999)
    private Integer airLockoutDuration;
    @Min(0)
    @Max(999999)
    private Integer airRecoveryDuration;
    @JsonIgnore
    private boolean deleted;
    private BigDecimal distance;
    @NotEmpty
    private List<@ValidRRule String> schedules;
    @Valid
    @NotNull
    private CapacityProfile capacityProfile;
    @Valid
    @NotNull
    private MeasurementUnits measurementUnits;
}
