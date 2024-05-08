package com.quincus.shipment.api.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class FilterResult {
    private long totalElements;
    private int page;
    private int totalPages;
    private List<?> result;
    private Filter filter;

    public FilterResult(List<?> result, Filter filter) {
        this.result = result;
        this.filter = filter;
    }
}
