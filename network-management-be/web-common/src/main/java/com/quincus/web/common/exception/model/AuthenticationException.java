package com.quincus.web.common.exception.model;

public class AuthenticationException extends QuincusException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String uuid) {
        super(message, uuid);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Object object, String message, Throwable cause) {
        super(object, message, cause);
    }
}
