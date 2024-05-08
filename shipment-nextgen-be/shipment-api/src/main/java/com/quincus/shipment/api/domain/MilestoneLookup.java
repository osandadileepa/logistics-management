package com.quincus.shipment.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MilestoneLookup {
    private String id;
    private String name;
    private String code;
    private String status;
    private String description;
    private String mileEquivalent;
    private String productModule;
    private String organizationId;
    private String organizationName;
    private String deletedTime;
}
