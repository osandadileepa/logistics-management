package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class ShipmentJourneyNotFoundException extends QuincusException {
    public ShipmentJourneyNotFoundException(String msg) {
        super(msg);
    }
}
