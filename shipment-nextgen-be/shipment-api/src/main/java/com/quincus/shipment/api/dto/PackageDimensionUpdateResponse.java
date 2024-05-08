package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class PackageDimensionUpdateResponse {
    private String organizationId;
    private String updateBy;
    private String updatedAt;
    private LocalDateTime updatedDate;
    private PackageDimension previousPackageDimension;
    private PackageDimension newPackageDimension;
    private Package shipmentPackage;
}
