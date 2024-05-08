package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class UserGroupNotAllowedException extends QuincusException {

    public UserGroupNotAllowedException(String msg) {
        super(msg);
    }
}