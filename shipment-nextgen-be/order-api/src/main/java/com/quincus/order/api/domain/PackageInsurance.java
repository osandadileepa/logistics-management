package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class PackageInsurance {
    private String id;
    private String packageId;
    private String insuranceTypeId;
    private String name;
    private String price;
    private String createdAt;
    private String updatedAt;
    private String organisationId;
    private String deletedAt;
}