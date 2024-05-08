package com.quincus.shipment.api.constant;

import lombok.Getter;

public enum SegmentType {
    FIRST_MILE("First mile"),
    MIDDLE_MILE("Middle mile"),
    LAST_MILE("Last mile");

    @Getter
    private final String label;

    SegmentType(String label) {
        this.label = label;
    }
}
