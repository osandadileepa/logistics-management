package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class Commodity {
    private String id;
    private String name;
    private boolean deleted;
    private String createdAt;
    private String updatedAt;
    private String organisationId;
    private String description;
    private String hsCode;
    private String code;
    private String note;
    private String packagingType;
}
