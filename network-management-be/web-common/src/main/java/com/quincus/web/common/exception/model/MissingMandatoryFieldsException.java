package com.quincus.web.common.exception.model;

public class MissingMandatoryFieldsException extends QuincusException {
    public MissingMandatoryFieldsException(String message) {
        super(message);
    }

    public MissingMandatoryFieldsException(String message, String uuid) {
        super(message, uuid);
    }

    public MissingMandatoryFieldsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingMandatoryFieldsException(Object object, String message, Throwable cause) {
        super(object, message, cause);
    }
}