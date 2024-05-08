package com.quincus.web.common.utility.annotation;

import org.slf4j.event.Level;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods in the application that need to have their execution time logged.
 * By annotating a method with @LogExecutionTime, the application will automatically log the total time taken to execute that method.
 * This annotation can be useful for performance profiling and identifying potential bottlenecks in the application.
 * When a method marked with @LogExecutionTime is invoked, the annotation's aspect intercepts the method call,
 * calculates the execution time, and logs it using a designated logger. This allows developers to easily track and
 * analyze the performance of specific methods in the application.
 *
 * @See com.quincus.web.common.utility.logging.LoggingAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
    Level level() default Level.DEBUG;

}
