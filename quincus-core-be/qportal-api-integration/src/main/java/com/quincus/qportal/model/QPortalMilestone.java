package com.quincus.qportal.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class QPortalMilestone {
    private String id;
    private String name;
    private String code;
    private String status;
    private String description;
    private String mileEquivalent;
    private String productModule;
    private String organizationId;
    private String organizationName;
    @JsonSetter("deleted_at")
    private String deletedTime;
}
