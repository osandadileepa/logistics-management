package com.quincus.qportal.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class QPortalNotificationRequest {
    private String code;
    private Set<String> partnerIds;
    private QPortalOperator operators;
    private QPortalParam params;

    public QPortalNotificationRequest(String code, Set<String> partnerIds, QPortalOperator operators) {
        this.code = code;
        this.partnerIds = partnerIds;
        this.operators = operators;
    }
}
