package com.quincus.shipment.api.filter;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static java.lang.Integer.MAX_VALUE;

@Data
public class PaginationFilter {
    @Min(1)
    @Max(100)
    private int size;
    @Min(1)
    @Max(MAX_VALUE)
    private int pageNumber;
    @Size(max = 100)
    private String sortBy;
    @Size(max = 10)
    private String sortDir;

    public int getPageNumber() {
        return this.pageNumber - 1;
    }
}
