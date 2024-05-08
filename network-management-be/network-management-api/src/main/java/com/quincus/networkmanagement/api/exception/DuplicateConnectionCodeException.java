package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;

public class DuplicateConnectionCodeException extends QuincusException {

    @Getter
    private final String field;

    @Getter
    private final String rejectedValue;

    public DuplicateConnectionCodeException(String rejectedValue) {
        super(String.format("Connection record with connection code `%s` already exists", rejectedValue));
        this.rejectedValue = rejectedValue;
        this.field = "connection_code";
    }
}
