package com.quincus.qportal.model;

import lombok.Data;

@Data
public class QPortalOrganizationChannel {
    private String id;
    private String channel;
    private String status;
    private String notificationsLimit;
    private String notificationsCounter;
}
