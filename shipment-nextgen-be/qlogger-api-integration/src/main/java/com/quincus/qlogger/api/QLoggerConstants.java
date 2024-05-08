package com.quincus.qlogger.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class QLoggerConstants {
    public static final String AUTHORIZATION = "X-API-AUTHORIZATION";
    public static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    public static final String SHIPMENT_MODULE = "SHP";
    public static final String ROOT_NAME = "payload";
    public static final String SHIPMENT_CANCELLED_CUSTOM_DATA_TEXT = "Cancelled Shipment ";
}
