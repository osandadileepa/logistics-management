package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ConnectionNotFoundException extends QuincusException {
    public ConnectionNotFoundException(String message) {
        super(message);
    }
}