package com.quincus.shipment.api.exception;

import lombok.Getter;

public class CsvValidationException extends Exception {

    @Getter
    private final long recordNum;
    @Getter
    private final String baseErrorMsg;

    public CsvValidationException(String message, long recordNum) {
        super(message);
        this.recordNum = recordNum;
        this.baseErrorMsg = message;
    }
}
