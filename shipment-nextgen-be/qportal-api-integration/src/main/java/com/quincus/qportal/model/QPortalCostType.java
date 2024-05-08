package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalCostType {
    private String id;
    private String name;
    private String description;
    private String isDefault;
    private String category;
    private String proof;
    private String status;
    private String[] tags;
}
