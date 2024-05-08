package com.quincus.qportal.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QPortalNotification {
    private String code;
    private List<String> partnerIds;
    private QPortalOperator operators;
    private QPortalParam params;

    public QPortalNotification(String code, List<String> partnerIds, QPortalOperator operators) {
        this.code = code;
        this.partnerIds = partnerIds;
        this.operators = operators;
    }
}
