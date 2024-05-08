package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.serializer.InstantSerializer;
import com.quincus.shipment.api.validator.constraint.ValidCostCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ValidCostCategory
public class Cost {
    @UUID(required = false)
    private String id;
    @NotNull
    @Valid
    private CostType costType;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    private BigDecimal costAmount;
    @NotNull
    @Valid
    private Currency currency;
    @UUID(required = false)
    private String driverId;
    @Size(max = 256)
    private String driverName;
    @NotNull
    private LocalDateTime issuedDate;
    @NotBlank
    @Size(min = 3, max = 15)
    private String issuedTimezone;
    @Size(min = 1)
    private List<@Valid CostShipment> shipments;
    @Size(max = 5)
    private List<@Valid ProofOfCost> proofOfCost;
    @Size(max = 2000, message = "Must be maximum of 2000 characters.")
    private String remarks;
    @UUID(required = false)
    private String partnerId;
    @Size(max = 256)
    private String partnerName;
    @UUID(required = false)
    private String organizationId;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createTime;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant modifyTime;
    @Size(max = 50)
    private String createdBy;
    @Size(max = 50)
    private String modifiedBy;
    @Size(max = 3)
    private String createdTimezone;
    @Size(max = 50)
    private String source;
    @JsonIgnore
    private Set<String> locationExternalIds = new HashSet<>();

    public void addLocationExternalIds(String locationExternalId) {
        locationExternalIds.add(locationExternalId);
    }
}
