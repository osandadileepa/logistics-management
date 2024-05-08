package com.quincus.web.common.utility.logging;

import org.slf4j.MDC;

import java.util.Map;

public class MdcTaskWrapper implements Runnable {
    private final Runnable delegate;
    private final Map<String, String> mdcContextMap;

    public MdcTaskWrapper(Runnable delegate) {
        this.delegate = delegate;
        this.mdcContextMap = MDC.getCopyOfContextMap();
    }

    @Override
    public void run() {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            if (mdcContextMap != null) {
                MDC.setContextMap(mdcContextMap);
            }
            delegate.run();
        } finally {
            if (previous != null) {
                MDC.setContextMap(previous);
            } else {
                MDC.clear();
            }
        }
    }
}
