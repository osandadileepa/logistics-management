package com.quincus.shipment.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PackageDimensionUpdateRequestFromApiG {

    @NotBlank
    private String shipmentTrackingId;
    @NotNull
    @Valid
    private ValueOfGoods valueOfGoods;
    private String packageTypeName;

}
