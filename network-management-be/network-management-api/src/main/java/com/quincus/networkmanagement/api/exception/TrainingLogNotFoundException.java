package com.quincus.networkmanagement.api.exception;

import com.quincus.web.common.exception.model.QuincusException;

public class TrainingLogNotFoundException extends QuincusException {
    public TrainingLogNotFoundException(String message) {
        super(message);
    }
}