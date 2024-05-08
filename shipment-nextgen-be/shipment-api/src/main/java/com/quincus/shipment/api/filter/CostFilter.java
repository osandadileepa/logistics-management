package com.quincus.shipment.api.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CostFilter extends PaginationFilter {
    private List<String> keys;
    private List<String> costTypes;
    private List<String> vendors;
    private List<String> drivers;
    private CostDateRange incurredDateRange;
    @Valid
    private CostAmountRange costAmountRange;
}