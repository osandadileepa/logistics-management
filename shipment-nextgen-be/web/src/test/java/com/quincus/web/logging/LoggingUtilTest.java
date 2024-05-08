package com.quincus.web.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingUtilTest {

    private static final String MDC_UUID = "UUID";

    @BeforeEach
    public void setUp() {
        MDC.put(MDC_UUID, "test-transaction-id");
    }

    @Test
    void testGetTransactionId_ReturnsValueWhenMDCIsSet() {
        String result = LoggingUtil.getTransactionId();

        assertThat(result).isEqualTo("test-transaction-id");
    }

    @Test
    void testGetTransactionId_ReturnsEmptyOptionalWhenMDCIsNull() {
        MDC.clear();

        String result = LoggingUtil.getTransactionId();

        assertThat(result).isNull();
    }
}
