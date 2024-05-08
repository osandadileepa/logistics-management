package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidRRuleException extends QuincusException {
    public InvalidRRuleException(String msg) {
        super(msg);
    }
}
