package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class Archived {
    @Size(min = 1, max = 255)
    private String referenceId;
    @Size(min = 1, max = 255)
    private String className;
    @NotNull
    private String data;
    @UUID(required = false)
    private String organizationId;
}
