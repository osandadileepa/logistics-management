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
public class ServiceType {
    @UUID(required = false)
    private String id;
    @NotBlank
    @Size(max = 64, message = "Must be maximum of 64 characters")
    private String code;
    @NotBlank
    @Size(max = 128, message = "Must be maximum of 128 characters")
    private String name;
    @UUID(required = false)
    private String organizationId;
}
