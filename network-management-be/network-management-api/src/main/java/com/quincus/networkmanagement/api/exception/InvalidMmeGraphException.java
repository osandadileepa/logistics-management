package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidMmeGraphException extends QuincusException {
    public InvalidMmeGraphException(String message) {
        super(message);
    }
}
