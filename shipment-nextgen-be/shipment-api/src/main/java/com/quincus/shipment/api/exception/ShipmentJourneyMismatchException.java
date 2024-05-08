package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentJourneyMismatchException extends QuincusException {
    public ShipmentJourneyMismatchException(String msg) {
        super(msg);
    }
}
