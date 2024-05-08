package com.quincus.shipment.api.constant;

import com.quincus.shipment.api.exception.InvalidEnumValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum MeasurementUnit {
    IMPERIAL("imperial"),
    METRIC("metric");

    private String label;

    MeasurementUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static MeasurementUnit fromValue(String val) {
        if (StringUtils.isEmpty(val)) {
            return null;
        }
        return Arrays.stream(MeasurementUnit.values())
                .filter(e -> StringUtils.equalsIgnoreCase(e.toString(), val))
                .findAny().orElseThrow(() -> new InvalidEnumValueException(val, MeasurementUnit.class));
    }
}
