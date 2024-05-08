package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class MilestoneNotFoundException extends QuincusException {

    public MilestoneNotFoundException(String msg) {
        super(msg);
    }
}
