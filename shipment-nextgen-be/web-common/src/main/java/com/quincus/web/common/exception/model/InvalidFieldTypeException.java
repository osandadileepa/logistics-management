package com.quincus.web.common.exception.model;

public class InvalidFieldTypeException extends QuincusException {
    public InvalidFieldTypeException(String message) {
        super(message);
    }

    public InvalidFieldTypeException(String message, String uuid) {
        super(message, uuid);
    }

    public InvalidFieldTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFieldTypeException(Object object, String message, Throwable cause) {
        super(object, message, cause);
    }
}