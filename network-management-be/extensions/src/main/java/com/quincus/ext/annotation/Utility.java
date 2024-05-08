package com.quincus.ext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @Utility annotation is used to clearly indicate that the annotated method or class is intended for
 * testing purposes only and should not be used in production code. It serves as a visual reminder for developers
 * that the code is meant for testing and helps prevent its accidental use in production environments.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Utility {
}
