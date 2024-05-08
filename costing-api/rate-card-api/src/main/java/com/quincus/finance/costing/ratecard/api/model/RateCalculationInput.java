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
public class RateCalculationInput {
    @NotNull
    private String rateCardId;
    @NotNull
    private BigDecimal weightValue;
    @NotNull
    private BigDecimal distanceValue;
    @NotNull
    private BigDecimal goodValue;
}
