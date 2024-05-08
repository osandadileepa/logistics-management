package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class NetworkLaneException extends QuincusException {
    private static final String NETWORKLANE_FIELD = "networklane.%s";
    @Getter
    private final List<String> errors;

    public NetworkLaneException() {
        super("Invalid Network Lane");
        errors = new ArrayList<>();
    }

    public void addError(String error, String field) {
        String formattedErrorMessage = String.format(NETWORKLANE_FIELD, field);
        errors.add(String.format("%s %s", formattedErrorMessage, error));
    }
}
