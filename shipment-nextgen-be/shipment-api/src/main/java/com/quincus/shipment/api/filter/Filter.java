package com.quincus.shipment.api.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Filter {
    private int page;
    private int perPage;
    private String key;
    private String sortBy;
    private String sortDir;
}
