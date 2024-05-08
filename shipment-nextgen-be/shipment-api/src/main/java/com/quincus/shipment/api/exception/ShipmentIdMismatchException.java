package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentIdMismatchException extends QuincusException {
    public ShipmentIdMismatchException(String msg) {
        super(msg);
    }
}

