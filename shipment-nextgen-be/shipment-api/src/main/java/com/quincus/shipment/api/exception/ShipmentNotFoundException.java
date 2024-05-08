package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentNotFoundException extends QuincusException {
    public ShipmentNotFoundException(String msg) {
        super(msg);
    }
}
