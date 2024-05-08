package com.quincus.core.impl.exception;


public class JobCancellationNotAllowedException extends QuincusException {
    public JobCancellationNotAllowedException(String msg) {
        super(msg);
    }
}
