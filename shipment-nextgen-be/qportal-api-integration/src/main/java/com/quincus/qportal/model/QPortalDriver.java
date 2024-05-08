package com.quincus.qportal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QPortalDriver extends QPortalModel {
    private String isBelongsToPartner;
    private String firstName;
    private String lastName;

    @Override
    public String getName() {
        return this.name != null ? this.name : firstName + " " + lastName;
    }
}
