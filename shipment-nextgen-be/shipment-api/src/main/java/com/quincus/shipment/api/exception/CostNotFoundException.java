package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class CostNotFoundException extends QuincusException {
    public CostNotFoundException(String msg) {
        super(msg);
    }
}