package com.quincus.shipment.api.filter;

import lombok.Data;

import javax.validation.constraints.Digits;
import java.math.BigDecimal;

@Data
public class CostAmountRange {
    @Digits(integer = 15, fraction = 2)
    private BigDecimal minCostAmount;
    @Digits(integer = 15, fraction = 2)
    private BigDecimal maxCostAmount;
}
