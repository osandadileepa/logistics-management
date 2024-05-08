package com.quincus.shipment.api.constant;

public enum UnitOfMeasure {
    KM("km"),
    METER("m"),
    MILE("mi"),
    MINUTE("min"),
    HOUR("hr");

    private final String label;

    UnitOfMeasure(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

