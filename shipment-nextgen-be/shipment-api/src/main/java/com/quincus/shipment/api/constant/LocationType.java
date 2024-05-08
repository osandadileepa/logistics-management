package com.quincus.shipment.api.constant;

import com.quincus.shipment.api.exception.InvalidEnumValueException;

public enum LocationType {
    COUNTRY(1),
    STATE(2),
    CITY(3),
    FACILITY(4);

    private final int value;

    LocationType(final int value) {
        this.value = value;
    }

    public static LocationType fromValue(int enumValue) {
        for (LocationType enumKey : LocationType.values()) {
            if (enumKey.value == enumValue) {
                return enumKey;
            }
        }
        throw new InvalidEnumValueException(enumValue, LocationType.class);
    }

    public int value() {
        return value;
    }

}
