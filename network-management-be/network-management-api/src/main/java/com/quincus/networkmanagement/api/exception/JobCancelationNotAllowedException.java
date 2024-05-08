package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class JobCancelationNotAllowedException extends QuincusException {
    public JobCancelationNotAllowedException(String msg) {
        super(msg);
    }
}

