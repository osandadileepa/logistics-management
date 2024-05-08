package com.quincus.karate.automation.utils;

import com.linecorp.armeria.internal.shaded.guava.net.UrlEscapers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class DataUtils {

    public static final String URL_SPACE_ENCODING = "%20";

    private DataUtils() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static long randomNumber() {
        long leftLimit = 10000000L;
        long rightLimit = 99999999L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    public static String getFormattedDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    public static String getFormattedDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return now.format(formatter);
    }

    public static String getOffsetDateTimeMinusDays(int days) {
        return OffsetDateTime.now().minusDays(days).toString();
    }

    public static String getOffsetDateTimePlusDays(int days) {
        return OffsetDateTime.now().plusDays(days).toString();
    }

    public static String getDateBefore() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -2);
        return sdf.format(cal.getTime());
    }

    public static String toUpperCase(String value) {
        return value.toUpperCase();
    }

    public static String toLowerCase(String value) {
        return value.toLowerCase();
    }

    @SuppressWarnings("java:S2925")
    public static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public static String decodeUrl(String url) {
        return UrlEscapers.urlFragmentEscaper().escape(url);
    }

    public static List<String> toList(String... values) {
        return List.of(values);
    }

}
