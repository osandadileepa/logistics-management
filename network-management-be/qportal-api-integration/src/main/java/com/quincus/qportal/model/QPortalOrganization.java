package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class QPortalOrganization {
    private String id;
    private String name;
    private String code;
    private String status;
    private String ssoProvider;
    private boolean isSsoEnabled;
    @JsonProperty("organisation_channels")
    private List<QPortalOrganizationChannel> organizationChannels;
    @JsonProperty("organisations_module_items")
    private List<QPortalOrganizationModuleItem> organizationModuleItems;
}
