package com.quincus.shipment.impl.helper;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class FieldUtil {

    private static final String CAMEL_TO_SNAKE_REGEX = "([a-z])([A-Z]+)";
    private static final String CAMEL_TO_SNAKE_REPLACEMENT = "$1_$2";

    public static String camelToSnake(String str) {
        str = str.replaceAll(CAMEL_TO_SNAKE_REGEX, CAMEL_TO_SNAKE_REPLACEMENT).toLowerCase();

        return str;
    }
}
