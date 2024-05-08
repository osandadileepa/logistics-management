package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.TriggeredFrom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Package {
    @UUID(required = false)
    private String id;
    @Size(max = 48)
    private String refId;
    @NotBlank
    @Size(max = 45, message = "Must be maximum of 45 characters.")
    private String type;
    @Size(max = 48, message = "Must be maximum of 48 characters.")
    private String typeRefId;
    @Size(max = 4, message = "Must be maximum of 4 characters.")
    @NotBlank
    private String currency;
    @DecimalMin(value = "0.0")
    @Digits(fraction = 4, integer = 16)
    private BigDecimal totalValue;
    @NotNull
    @Valid
    private PackageDimension dimension;
    @NotNull
    @NotEmpty
    private List<@Valid Commodity> commodities;
    @NotNull
    @Valid
    private PricingInfo pricingInfo;
    @Size(max = 45, message = "Must be maximum of 45 characters")
    private String value;
    private LocalDateTime readyTime;
    @Size(max = 45, message = "Must be maximum of 45 characters")
    private String code;
    @NotNull
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private Long totalItemsCount;
    private TriggeredFrom source;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Package that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
