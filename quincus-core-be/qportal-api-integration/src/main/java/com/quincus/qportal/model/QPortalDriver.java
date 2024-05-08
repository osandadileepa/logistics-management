package com.quincus.qportal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QPortalDriver extends QPortalModel {
    private String isBelongsToPartner;
}
