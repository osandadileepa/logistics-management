package com.quincus.finance.costing.weightcalculation.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonRootName("data")
public class WeightCalculationOutput {
    private BigDecimal chargeableWeight;
    private BigDecimal actualWeight;
    private BigDecimal volumeWeight;
    private String ruleApplied;
    private String ruleId;
}