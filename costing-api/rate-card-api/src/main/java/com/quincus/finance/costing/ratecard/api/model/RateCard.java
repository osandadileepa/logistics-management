package com.quincus.finance.costing.ratecard.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonRootName("data")
public class RateCard {
    private String id;
    @NotNull
    private RateCardCalculationType calculationType;
    @NotNull
    private BigDecimal rateValue;
    private BigDecimal min;
    private BigDecimal max;
}
