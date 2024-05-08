package com.quincus.authentication.exception;

public class AuthenticationApiException extends RuntimeException {

    public AuthenticationApiException(String message) {
        super(message);
    }

    public AuthenticationApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
