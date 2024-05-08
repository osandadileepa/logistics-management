package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class LocationMessageException extends QuincusException {
    public LocationMessageException(String msg, String uuid) {
        super(msg, uuid);
    }
}
