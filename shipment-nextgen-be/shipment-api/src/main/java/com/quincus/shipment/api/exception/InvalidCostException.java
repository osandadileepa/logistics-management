package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidCostException extends QuincusException {
    public InvalidCostException(String msg) {
        super(msg);
    }
}
