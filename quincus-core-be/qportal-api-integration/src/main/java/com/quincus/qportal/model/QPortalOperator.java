package com.quincus.qportal.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QPortalOperator {
    private QPortalOperatorUser senders;
    private QPortalOperatorUser drivers;
    private QPortalOperatorUser consignees;
    private QPortalOperatorUser others;
}
