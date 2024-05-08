package com.quincus.web.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class LoggingUtil {
    protected static final String MDC_UUID = "UUID";

    private LoggingUtil() {
    }

    public static String getTransactionId() {
        try {
            return MDC.get(MDC_UUID);
        } catch (Exception ex) {
            log.debug("Cannot get Transaction ID or UUID: {}", ex.getMessage());
        }
        return null;
    }

}
