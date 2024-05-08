package com.quincus.karate.automation.utils;

import java.util.UUID;

public final class DataUtils {

    private DataUtils() {

    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }
    
}
