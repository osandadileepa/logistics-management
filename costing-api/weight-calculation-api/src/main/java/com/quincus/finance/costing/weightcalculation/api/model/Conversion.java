package com.quincus.finance.costing.weightcalculation.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Conversion implements Serializable {
    private BigDecimal from;
    private BigDecimal to;
    private BigDecimal result;
}