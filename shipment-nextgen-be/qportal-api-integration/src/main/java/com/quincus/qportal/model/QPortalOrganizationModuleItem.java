package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalOrganizationModuleItem {
    private String id;
    private String version;
    private QPortalModuleItem moduleItem;
}
