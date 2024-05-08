package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class QPortalSyncFailedException extends QuincusException {
    public QPortalSyncFailedException(String message) {
        super(message);
    }
}