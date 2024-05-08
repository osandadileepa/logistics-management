package com.quincus.shipment.kafka.consumers.interceptor;

import org.springframework.web.context.request.RequestAttributes;

import java.util.HashMap;
import java.util.Map;

public class KafkaRequestAttribute implements RequestAttributes {

    private final Map<String, Object> requestAttributeMap = new HashMap<>();

    @Override
    public Object getAttribute(String name, int scope) {
        return scope == SCOPE_REQUEST ? this.requestAttributeMap.get(name) : null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        if (scope == SCOPE_REQUEST) {
            this.requestAttributeMap.put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            this.requestAttributeMap.remove(name);
        }
    }

    @Override
    public String[] getAttributeNames(int scope) {
        return scope == SCOPE_REQUEST ? (String[]) this.requestAttributeMap.keySet().toArray(new String[0]) : new String[0];
    }

    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        // Not supported
    }

    public Object resolveReference(String key) {
        // Not supported
        return null;
    }

    public String getSessionId() {
        return null;
    }

    public Object getSessionMutex() {
        return null;
    }
}