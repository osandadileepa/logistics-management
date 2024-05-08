package com.quincus.finance.costing.weightcalculation.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@JsonRootName("data")
public class CheckRoundingLogicOutput {
    private BigDecimal result;
}
