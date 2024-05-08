package com.quincus.web.common.utility.logging;

import ch.qos.logback.classic.Logger;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    private static final String LOG_EXECUTION_TIME = "{}.{} executed with duration of {} ms.";
    private static final String LOG_ERROR_MESSAGE = "Error occurred in method {}.{}: {}";
    private static final String MDC_DURATION = "duration";
    private static final String MDC_CLASS = "class";
    private static final String MDC_METHOD_NAME = "methodName";

    @Value("${logging.execution-time:false}")
    private boolean isLoggingEnabled;

    /**
     * This annotation is used to mark methods in a Java application that need to have their execution time logged.
     * By annotating a method with {@code @LogExecutionTime}, the application will automatically log the total time taken to execute that method.
     * <p>This annotation uses Around advice, which runs before and after the method execution, to calculate and log the execution time.
     * The advice is applied to all methods that are annotated with {@code @LogExecutionTime}.
     * <p>It is useful for performance profiling and identifying potential bottlenecks in the application.
     */
    @Around("@annotation(com.quincus.web.common.utility.annotation.LogExecutionTime)")
    public Object calculateAndLogMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (!isLoggingEnabled) {
            return proceedingJoinPoint.proceed();
        }

        final Level logLevel = getLogLevel(proceedingJoinPoint);
        if (!isPackageLogLevelEnabled(getPackageName(proceedingJoinPoint), logLevel)) {
            return proceedingJoinPoint.proceed();
        }

        final MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        final String methodName = methodSignature.getName();
        final long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Exception e) {
            log(logLevel, LOG_ERROR_MESSAGE, proceedingJoinPoint.getTarget().getClass(), methodName, e.getMessage());
            throw e;
        } finally {
            final long endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;
            MDC.put(MDC_DURATION, String.valueOf(duration));
            MDC.put(MDC_CLASS, proceedingJoinPoint.getTarget().getClass().toString());
            MDC.put(MDC_METHOD_NAME, methodName);
            log(logLevel, LOG_EXECUTION_TIME, proceedingJoinPoint.getTarget().getClass(), methodName, duration);
            removeMDCFields();
        }

        return result;
    }

    private void removeMDCFields() {
        MDC.remove(MDC_DURATION);
        MDC.remove(MDC_CLASS);
        MDC.remove(MDC_METHOD_NAME);
    }

    private Level getLogLevel(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        LogExecutionTime annotation = methodSignature.getMethod().getAnnotation(LogExecutionTime.class);
        return annotation.level();
    }

    private boolean isPackageLogLevelEnabled(String packageName, Level level) {
        Logger packageLogger = (Logger) LoggerFactory.getLogger(packageName);
        return switch (level) {
            case TRACE -> packageLogger.isTraceEnabled();
            case DEBUG -> packageLogger.isDebugEnabled();
            case INFO -> packageLogger.isInfoEnabled();
            case WARN -> packageLogger.isWarnEnabled();
            case ERROR -> packageLogger.isErrorEnabled();
        };
    }

    private void log(Level level, String message, Object... args) {
        switch (level) {
            case TRACE -> log.trace(message, args);
            case DEBUG -> log.debug(message, args);
            case INFO -> log.info(message, args);
            case WARN -> log.warn(message, args);
            case ERROR -> log.error(message, args);
        }
    }

    private String getPackageName(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getName();
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return className.substring(0, lastDotIndex);
        }
        return "";
    }
}
