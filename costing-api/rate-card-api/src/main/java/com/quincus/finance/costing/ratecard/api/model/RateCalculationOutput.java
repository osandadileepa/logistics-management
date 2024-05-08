package com.quincus.finance.costing.ratecard.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonRootName("data")
public class RateCalculationOutput {
    private BigDecimal result;
    private String rateCardId;
}
