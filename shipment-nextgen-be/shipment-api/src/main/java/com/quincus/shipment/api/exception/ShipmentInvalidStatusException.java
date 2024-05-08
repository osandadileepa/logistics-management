package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentInvalidStatusException extends QuincusException {
    public ShipmentInvalidStatusException(String msg) {
        super(msg);
    }
}

