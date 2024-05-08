package com.quincus.core.impl.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class DateTimeUtil {
    private static final List<String> LOCAL_DATE_TIME_PATTERNS = List.of("yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss");
    private static final List<String> ZONED_DATE_TIME_PATTERNS = List.of("yyyy-MM-dd HH:mm:ss O",
            "yyyy-MM-dd HH:mm:ss VV",
            "yyyy-MM-dd HH:mm:ss Z");
    private static final String ZONE_GMT = "GMT";
    private static final String ZONE_UTC = "UTC";
    private static final String ZONE_UTC_ABBR = "UT";
    private static final String MILLIS_READABLE_FMT = "%d min, %d sec, %d ms";

    public static LocalDateTime parseLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }

        //Try parse against each accepted pattern then end early on successful parse
        LocalDateTime localDateTime = null;
        for (String acceptedPattern : LOCAL_DATE_TIME_PATTERNS) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(acceptedPattern);
            try {
                localDateTime = LocalDateTime.parse(dateTimeStr, dtf);
                break;
            } catch (DateTimeParseException e) {
                //continue to next format
            }
        }
        return localDateTime;
    }

    public static ZonedDateTime parseZonedDateTime(String timeStr) {
        if (timeStr == null) {
            return null;
        }

        //Try parse against each accepted pattern then end early on successful parse
        ZonedDateTime zonedDateTime = null;
        for (String acceptedPattern : ZONED_DATE_TIME_PATTERNS) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(acceptedPattern);
            try {
                zonedDateTime = ZonedDateTime.parse(timeStr, dtf);
                break;
            } catch (DateTimeParseException e) {
                //continue to next format
            }
        }
        return zonedDateTime;
    }

    public static Instant parseInstant(String timeStr) {
        if (timeStr == null) {
            return null;
        }
        return Instant.parse(timeStr);
    }

    public static ZoneId parseZoneId(String timezoneStr) {
        if (timezoneStr == null) {
            return null;
        }

        ZoneOffset zoneOffset;
        String offsetStr;
        String prefixStr;
        int plusOffsetIdx = timezoneStr.lastIndexOf('+');
        int minusOffsetIdx = timezoneStr.lastIndexOf('-');
        if (plusOffsetIdx >= 0) {
            offsetStr = timezoneStr.substring(plusOffsetIdx);
            zoneOffset = ZoneOffset.of(offsetStr);
            prefixStr = timezoneStr.substring(0, plusOffsetIdx).trim();
            return ZoneId.ofOffset(prefixStr, zoneOffset);
        }
        if (minusOffsetIdx >= 0) {
            offsetStr = timezoneStr.substring(minusOffsetIdx);
            zoneOffset = ZoneOffset.of(offsetStr);
            prefixStr = timezoneStr.substring(0, minusOffsetIdx).trim();
            return ZoneId.ofOffset(prefixStr, zoneOffset);
        }
        if (timezoneStr.startsWith(ZONE_UTC_ABBR) || timezoneStr.equalsIgnoreCase(ZONE_GMT)) {
            return ZoneId.ofOffset(timezoneStr, ZoneOffset.UTC);
        }
        //e.g.: "EST", "PST"
        return ZoneId.of(timezoneStr, ZoneId.SHORT_IDS);
    }

    public static String convertTimezoneToUtc(String timezone, LocalDateTime refDateTime) {
        if (timezone == null) {
            return null;
        }

        if (timezone.startsWith(ZONE_GMT)) {
            return timezone.replace(ZONE_GMT, ZONE_UTC);
        }

        if (timezone.startsWith(ZONE_UTC)) {
            return timezone;
        }

        //For other timezones like PST, need to account for rules such as Daylight Savings Time
        if (!timezone.startsWith(ZONE_UTC_ABBR)) {
            ZoneId zoneId = ZoneId.of(timezone, ZoneId.SHORT_IDS);
            ZoneOffset offset = zoneId.getRules().getOffset(refDateTime);
            return String.format("UTC%s", offset);
        }

        return timezone.replace(ZONE_UTC_ABBR, ZONE_UTC);
    }

    public static String getTimeZoneOffset(String date, String zoneId) {
        ZonedDateTime zonedDateTime = parseZonedDateTime(date);
        if (zonedDateTime == null || StringUtils.isEmpty(zoneId)) {
            return null;
        }
        try {
            return ZONE_UTC + zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId.trim())).getOffset();
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative");
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        return String.format(MILLIS_READABLE_FMT, minutes, seconds, millis);
    }

    public static boolean isTodayOrPastDate(String dateStr) {
        OffsetDateTime dateTime = OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime currentDateTime = OffsetDateTime.now();

        return dateTime.toLocalDate().isEqual(currentDateTime.toLocalDate()) || dateTime.isBefore(currentDateTime);
    }

    public static ZonedDateTime localeDateTimeToZoneDateTime(LocalDateTime localeDateTime, String timeZone) {
        if (StringUtils.isBlank(timeZone) || localeDateTime == null) {
            return null;
        }
        try {
            ZoneId startZoneId = DateTimeUtil.parseZoneId(timeZone);
            return ZonedDateTime.of(localeDateTime, startZoneId);
        } catch (Exception e) {
            return null;
        }
    }
}
