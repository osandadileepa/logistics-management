package com.quincus.shipment.api.filter;

import com.quincus.ext.annotation.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ServiceTypeFilter {
    @UUID(required = false)
    private String id;
    @Size(max = 128)
    private String name;
}