package com.quincus.shipment.api.constant;

import com.quincus.shipment.api.exception.InvalidEnumValueException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum PackageTypeMetricUnit {
    CM("Centimeters"),
    KG("Kilogram"),
    IN("Inches"),
    LB("Pound"),
    M3("Cubic Meter"),
    M("Meter"),
    MM("Millimeter"),
    FT3("Cubic Feet");

    @Getter
    private final String code;

    PackageTypeMetricUnit(String code) {
        this.code = code;
    }

    public static PackageTypeMetricUnit fromValue(String enumValue) {
        if (enumValue == null) {
            return null;
        }
        for (PackageTypeMetricUnit packageTypeMetricUnit : values()) {
            if (StringUtils.equalsIgnoreCase(packageTypeMetricUnit.code, enumValue)) {
                return packageTypeMetricUnit;
            }
        }
        throw new InvalidEnumValueException(enumValue, PackageTypeMetricUnit.class);
    }
}
