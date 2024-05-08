package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class NetworkLaneNotFoundException extends QuincusException {
    public NetworkLaneNotFoundException(String msg) {
        super(msg);
    }
}
