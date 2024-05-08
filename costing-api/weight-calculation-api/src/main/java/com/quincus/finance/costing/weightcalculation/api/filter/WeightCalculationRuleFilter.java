package com.quincus.finance.costing.weightcalculation.api.filter;

import com.quincus.finance.costing.common.web.model.filter.CostingApiFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeightCalculationRuleFilter extends CostingApiFilter {
    private String name;
    private String includeDefaultRules;
}
