package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidLocationException extends QuincusException {
    public InvalidLocationException(String msg, String transactionId) {
        super(msg, transactionId);
    }

    public InvalidLocationException(String msg) {
        super(msg);
    }
}
