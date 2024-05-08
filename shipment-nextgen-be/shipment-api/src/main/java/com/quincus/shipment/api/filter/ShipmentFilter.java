package com.quincus.shipment.api.filter;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
public class ShipmentFilter extends SearchFilter {
    @Min(1)
    @Max(100)
    private int size;
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int pageNumber;

    public int getPageNumber() {
        return this.pageNumber - 1;
    }
}
