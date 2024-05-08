package com.quincus.finance.costing.weightcalculation.api.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpecialVolumeWeightRule {
    private String fileUploaded;
    private String customFormula;
    private List<Conversion> conversions = new ArrayList<>();
}
