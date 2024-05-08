package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class SegmentNotFoundException extends QuincusException {
    public SegmentNotFoundException(String msg, String uuid) {
        super(msg, uuid);
    }

    public SegmentNotFoundException(String msg) {
        super(msg);
    }
}
