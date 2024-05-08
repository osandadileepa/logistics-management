package com.quincus.finance.costing.common.web.model;

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
public class RoundingLogic {
    @NotNull
    private BigDecimal roundTo;
    @NotNull
    private BigDecimal threshold;
}