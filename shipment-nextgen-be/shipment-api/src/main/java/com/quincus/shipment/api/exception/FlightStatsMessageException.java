package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class FlightStatsMessageException extends QuincusException {
    public FlightStatsMessageException(String msg, String uuid) {
        super(msg, uuid);
    }
}
