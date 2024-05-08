package com.quincus.shipment.api.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchFilterResult<T> {
    private List<T> result;
}
