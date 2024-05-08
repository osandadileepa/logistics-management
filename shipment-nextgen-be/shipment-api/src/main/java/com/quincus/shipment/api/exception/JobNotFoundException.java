package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class JobNotFoundException extends QuincusException {
    public JobNotFoundException(String msg) {
        super(msg);
    }
}
