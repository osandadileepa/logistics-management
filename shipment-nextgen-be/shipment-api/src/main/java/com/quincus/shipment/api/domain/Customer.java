package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @UUID(required = false)
    private String id;
    @NotBlank
    @Size(max = 64)
    private String code;
    @Size(max = 64)
    private String name;
    @Size(max = 48)
    private String organizationId;
}
