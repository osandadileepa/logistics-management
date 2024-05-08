package com.quincus.shipment.api.constant;

import com.quincus.shipment.api.exception.InvalidEnumValueException;
import org.apache.commons.lang3.StringUtils;

public enum PackageDimensionUpdateImportHeader {
    SHIPMENT_ID("Shipment ID"),
    PACKAGING_TYPE("Packaging Type"),
    UNIT("Unit"),
    HEIGHT("Height"),
    WIDTH("Width"),
    LENGTH("Length"),
    WEIGHT("Weight");

    private final String value;

    PackageDimensionUpdateImportHeader(final String value) {
        this.value = value;
    }

    public static void fromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        for (PackageDimensionUpdateImportHeader header : PackageDimensionUpdateImportHeader.values()) {
            if (StringUtils.equalsIgnoreCase(header.value, value)) {
                return;
            }
        }
        throw new InvalidEnumValueException(value, PackageDimensionUpdateImportHeader.class);
    }

    @Override
    public String toString() {
        return value;
    }

}
