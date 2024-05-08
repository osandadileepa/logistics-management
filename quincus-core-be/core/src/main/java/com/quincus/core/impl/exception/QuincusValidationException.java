package com.quincus.core.impl.exception;

public class QuincusValidationException extends QuincusException {
    public QuincusValidationException(String message) {
        super(message);
    }

    public QuincusValidationException(String message, String uuid) {
        super(message, uuid);
    }

    public QuincusValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
