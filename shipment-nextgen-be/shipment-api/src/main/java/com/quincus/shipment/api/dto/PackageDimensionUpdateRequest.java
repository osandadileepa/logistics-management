package com.quincus.shipment.api.dto;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.TriggeredFrom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PackageDimensionUpdateRequest {
    @NotBlank
    private String organizationId;
    @NotBlank
    @UUID
    private String userId;
    private String userLocationId;
    @DecimalMin(value = "0.0")
    private BigDecimal height;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal length;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal width;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal grossWeight;
    @Size(max = 48, message = "Maximum of 48 characters allowed.")
    private String shipmentTrackingId;
    private MeasurementUnit measurementUnit;
    private String packageTypeId;
    private String packageTypeName;
    private TriggeredFrom source;
}
