package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class VehicleType {
    @UUID
    private String id;
    @Size(max = 256)
    private String name;
    @Size(max = 256)
    private String nameI18n;
}