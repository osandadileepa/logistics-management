package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class NodeNotFoundException extends QuincusException {
    public NodeNotFoundException(String message) {
        super(message);
    }
}
