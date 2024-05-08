package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class InvalidMilestoneException extends QuincusException {
    public InvalidMilestoneException(String msg, String uuid) {
        super(msg, uuid);
    }

    public InvalidMilestoneException(String msg) {
        super(msg);
    }
}
