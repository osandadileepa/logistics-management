package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class LocationHierarchyDuplicateException extends QuincusException {
    public LocationHierarchyDuplicateException(String msg) {
        super(msg);
    }
}
