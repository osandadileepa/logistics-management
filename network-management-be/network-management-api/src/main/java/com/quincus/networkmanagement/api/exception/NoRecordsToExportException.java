package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class NoRecordsToExportException extends QuincusException {
    public NoRecordsToExportException(String message) {
        super(message);
    }
}