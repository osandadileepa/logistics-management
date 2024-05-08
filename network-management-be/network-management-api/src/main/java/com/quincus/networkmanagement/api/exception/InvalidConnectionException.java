package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidConnectionException extends QuincusException {
    public InvalidConnectionException(String message) {
        super(message);
    }
}
