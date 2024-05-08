package com.quincus.web.common.exception.model;

import lombok.Getter;

import java.util.UUID;

public class QuincusException extends RuntimeException {

    @Getter
    private final String uuid;
    @Getter
    private final transient Object object;

    public QuincusException(String message) {
        super(message);
        this.uuid = UUID.randomUUID().toString();
        this.object = null;
    }

    public QuincusException(String message, String uuid) {
        super(message);
        this.uuid = uuid;
        this.object = null;
    }

    public QuincusException(String message, Throwable cause) {
        super(message, cause);
        this.uuid = UUID.randomUUID().toString();
        this.object = null;
    }

    public QuincusException(Object object, String message, Throwable cause) {
        super(message, cause);
        this.uuid = UUID.randomUUID().toString();
        this.object = object;
    }

}
