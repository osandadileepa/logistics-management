package com.quincus.networkmanagement.impl.web.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
public class NetworkManagementExceptionHelper {
    public static String getFieldName(List<JsonMappingException.Reference> path) {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < path.size(); i++) {
            JsonMappingException.Reference ref = path.get(i);
            if (ref.getFieldName() != null) {
                if (i > 0) {
                    stringBuilder.append(".");
                }
                stringBuilder.append(ref.getFieldName());
            } else if (ref.getIndex() >= 0) {
                stringBuilder.append("[");
                stringBuilder.append(ref.getIndex());
                stringBuilder.append("]");
            }
        }

        return stringBuilder.toString();
    }

    public static String camelToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(input.charAt(0)));

        for (int i = 1; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append('_');
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }
}
