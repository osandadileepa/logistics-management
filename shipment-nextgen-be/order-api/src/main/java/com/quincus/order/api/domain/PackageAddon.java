package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class PackageAddon {
    private String id;
    private String packageId;
    private String addonTypeId;
    private String name;
    private String price;
    private String createdAt;
    private String updatedAt;
    private String organisationId;
    private String deletedAt;
}