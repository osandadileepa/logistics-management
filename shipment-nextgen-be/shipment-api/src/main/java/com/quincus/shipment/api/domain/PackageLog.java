package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.TriggeredFrom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PackageLog {
    private String id;
    @NotNull
    private String shipmentId;
    @NotNull
    private String packageId;
    private TriggeredFrom source;
    @NotNull
    private MeasurementUnit measurementUnit;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal volumeWeight;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal grossWeight;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal chargeableWeight;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal length;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal width;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal height;
    private boolean custom;

    // Overridden methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageLog that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
