package com.quincus.shipment.impl.web.exception.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(@NonNull Throwable e, Method method, @NonNull Object... params) {
        log.error(String.format("Error Occurred while executing method %s asynchronously. Error: %s", method.getName(),
                e.getMessage()), e);
    }
}
