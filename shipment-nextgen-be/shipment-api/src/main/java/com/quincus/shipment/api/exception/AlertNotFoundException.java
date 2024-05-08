package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class AlertNotFoundException extends QuincusException {

    public AlertNotFoundException(String msg) {
        super(msg);
    }
}
