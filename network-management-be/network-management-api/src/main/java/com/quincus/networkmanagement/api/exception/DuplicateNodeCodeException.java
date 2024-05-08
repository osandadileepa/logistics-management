package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;

public class DuplicateNodeCodeException extends QuincusException {

    @Getter
    private final String field;

    @Getter
    private final String rejectedValue;

    public DuplicateNodeCodeException(String rejectedValue) {
        super(String.format("Node record with node code `%s` already exists", rejectedValue));
        this.rejectedValue = rejectedValue;
        this.field = "node_code";
    }
}
