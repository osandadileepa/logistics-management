package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PricingInfo {
    @UUID(required = false)
    private String id;
    @UUID(required = false)
    private String externalId;
    @NotBlank
    @Size(max = 64)
    private String currency;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal baseTariff;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal serviceTypeCharge;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal surcharge;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal insuranceCharge;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal extraCareCharge;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal discount;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal tax;
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal cod;
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 14, fraction = 3)
    private BigDecimal total;
}
