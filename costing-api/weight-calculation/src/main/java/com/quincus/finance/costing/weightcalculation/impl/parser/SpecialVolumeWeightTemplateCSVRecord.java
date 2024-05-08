package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SpecialVolumeWeightTemplateCSVRecord {
    @CsvBindByName
    private BigDecimal from = BigDecimal.ZERO;
    @CsvBindByName
    private BigDecimal to = BigDecimal.ZERO;
    @CsvBindByName
    private BigDecimal result = BigDecimal.ZERO;
    @CsvBindByName
    private String formula;
}
