package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Commodity {
    @UUID(required = false)
    private String id;
    @UUID(required = false)
    private String externalId;
    @NotBlank
    @Size(max = 45, message = "Must be maximum of 45 characters.")
    private String name;
    @NotNull
    @Min(1)
    @Digits(integer = 10, fraction = 0)
    private Long quantity;
    @DecimalMin(value = "0.0")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal value;
    @Size(max = 10000, message = "Must be maximum of 10,000 characters.")
    private String description;
    @Size(max = 45, message = "Must be maximum of 45 characters.")
    private String code;
    @Size(max = 45, message = "Must be maximum of 45 characters.")
    private String hsCode;
    @Size(max = 10000, message = "Must be maximum of 10,000 characters.")
    private String note;
    @Size(max = 45, message = "Must be maximum of 45 characters.")
    private String packagingType;
}
