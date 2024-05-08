package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidTemplateException extends QuincusException {
    public InvalidTemplateException(String msg) {
        super(msg);
    }
}
