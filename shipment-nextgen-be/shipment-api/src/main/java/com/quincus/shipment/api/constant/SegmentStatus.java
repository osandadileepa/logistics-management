package com.quincus.shipment.api.constant;

import lombok.Getter;

public enum SegmentStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    @Getter
    private final String label;

    SegmentStatus(String label) {
        this.label = label;
    }
}
