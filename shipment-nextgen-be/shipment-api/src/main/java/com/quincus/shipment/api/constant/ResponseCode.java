package com.quincus.shipment.api.constant;

public enum ResponseCode {
    SCC0000("Success"),
    ERR0002("Incorrect type for field(s) %s"),
    ERR0003("Missing required field(s) %s"),
    ERR0004("Missing Reference %s"),
    ERR0005("Record %s not found "),
    ERR9999("%s");

    private String message;

    private ResponseCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
