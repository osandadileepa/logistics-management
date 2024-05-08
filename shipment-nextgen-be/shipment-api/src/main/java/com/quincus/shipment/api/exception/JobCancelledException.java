package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class JobCancelledException extends QuincusException {
    public JobCancelledException(String msg) {
        super(msg);
    }
}
