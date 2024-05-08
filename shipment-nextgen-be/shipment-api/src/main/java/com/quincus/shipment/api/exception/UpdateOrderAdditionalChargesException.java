package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class UpdateOrderAdditionalChargesException extends QuincusException {
    public UpdateOrderAdditionalChargesException(String msg) {
        super(msg);
    }
}