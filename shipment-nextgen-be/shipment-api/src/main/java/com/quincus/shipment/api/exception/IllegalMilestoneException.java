package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

import java.util.Map;
import java.util.stream.Collectors;

public class IllegalMilestoneException extends QuincusException {

    private static final String FIELDS_NOT_FOUND_MSG = "Milestone not saved because one or more of these fields are not found: %s";

    public IllegalMilestoneException(Map<String, String> notFoundFieldValuePairs, String uuid) {
        super(String.format(FIELDS_NOT_FOUND_MSG, getNotFoundFieldsAsString(notFoundFieldValuePairs)), uuid);
    }

    private static String getNotFoundFieldsAsString(Map<String, String> notFoundFieldValuePairs) {
        return notFoundFieldValuePairs.keySet().stream()
                .map(key -> String.format("%s = %s", key, notFoundFieldValuePairs.get(key)))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
