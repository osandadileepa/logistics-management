package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentJourneyException extends QuincusException {
    public ShipmentJourneyException(String msg) {
        super(msg);
    }
}
