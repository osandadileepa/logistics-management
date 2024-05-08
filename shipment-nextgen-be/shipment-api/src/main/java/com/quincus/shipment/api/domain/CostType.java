package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.CostCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CostType {
    @UUID
    private String id;
    @Size(max = 256)
    private String name;
    @Size(max = 256)
    private String description;
    @Valid
    private CostCategory category;
    @Size(max = 16)
    private String proof;
    @Size(max = 16)
    private String status;
}