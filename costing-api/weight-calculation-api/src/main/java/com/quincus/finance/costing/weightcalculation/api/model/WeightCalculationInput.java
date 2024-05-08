package com.quincus.finance.costing.weightcalculation.api.model;

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
public class WeightCalculationInput {

    private String ruleId;
    @NotNull
    private BigDecimal length;
    @NotNull
    private BigDecimal width;
    @NotNull
    private BigDecimal height;
    @NotNull
    private BigDecimal actualWeight;
    private String organizationId;
    private String partnerId;

}
