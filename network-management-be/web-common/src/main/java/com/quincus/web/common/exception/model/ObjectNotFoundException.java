package com.quincus.web.common.exception.model;

import lombok.Getter;

public class ObjectNotFoundException extends QuincusException {
    @Getter
    private final String name;
    private static final String EXCEPTION_MSG_FMT = "Cannot find %s %s.";

    public ObjectNotFoundException(String type, String name) {
        super(String.format(EXCEPTION_MSG_FMT, type, name));
        this.name = name;
    }

    public ObjectNotFoundException(Object object, String message, Throwable cause) {
        super(object, message, cause);
        this.name = message;
    }
}
