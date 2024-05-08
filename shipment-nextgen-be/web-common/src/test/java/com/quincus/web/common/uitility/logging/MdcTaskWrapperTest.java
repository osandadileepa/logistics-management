package com.quincus.web.common.uitility.logging;

import com.quincus.web.common.utility.logging.MdcTaskWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MdcTaskWrapperTest {
    private Map<String, String> originalMdcContextMap;

    @BeforeEach
    void setUp() {
        originalMdcContextMap = MDC.getCopyOfContextMap();
    }

    @Test
    void testMdcTaskWrapper() {
        Runnable delegate = mock(Runnable.class);

        Map<String, String> mdcContextMap = new HashMap<>();
        mdcContextMap.put("key1", "value1");
        mdcContextMap.put("key2", "value2");

        MDC.setContextMap(mdcContextMap);

        try {
            MdcTaskWrapper mdcTaskWrapper = new MdcTaskWrapper(delegate);

            mdcTaskWrapper.run();

            verify(delegate, times(1)).run();
        } finally {
            if (originalMdcContextMap != null) {
                MDC.setContextMap(originalMdcContextMap);
            } else {
                MDC.clear();
            }
        }
    }
}
