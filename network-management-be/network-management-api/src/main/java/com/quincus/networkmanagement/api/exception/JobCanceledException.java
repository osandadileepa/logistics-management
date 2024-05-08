package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class JobCanceledException extends QuincusException {
    public JobCanceledException(String msg) {
        super(msg);
    }
}
