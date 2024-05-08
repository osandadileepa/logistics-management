package com.quincus.web.common.exception.model;

import lombok.Getter;

public class InvalidFieldTypeException extends QuincusException {
    @Getter
    private final String field;

    public InvalidFieldTypeException(String message, String field) {
        super(message);
        this.field = field;
    }
}