package com.quincus.shipment.impl.web.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ShipmentExceptionHelper {

    public static String getFieldName(InvalidFormatException e) {

        StringBuilder path = new StringBuilder();

        for (int i = 0; i < e.getPath().size(); i++) {
            JsonMappingException.Reference ref = e.getPath().get(i);
            if (ref.getFieldName() != null) {
                if (i > 0) {
                    path.append(".");
                }
                path.append(toSnakeCase(ref.getFieldName()));
            } else if (ref.getIndex() >= 0) {
                path.append("[");
                path.append(ref.getIndex());
                path.append("]");
            }
        }

        return path.toString();
    }

    public static String toSnakeCase(String input) {
        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];
            if (i == chars.length - 1) {
                result.append(currentChar);
                continue;
            }

            char nextChar = chars[i + 1];
            result.append(currentChar);

            if (Character.isLowerCase(currentChar) && Character.isUpperCase(nextChar)) {
                result.append('_');
            }
        }

        return result.toString().toLowerCase();
    }

}
