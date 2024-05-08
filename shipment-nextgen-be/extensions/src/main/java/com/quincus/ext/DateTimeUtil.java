package com.quincus.ext;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.DateTimeException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class DateTimeUtil {
    public static final String ZONE_UTC = "UTC";

    private static final String UTC = "((?=UTC))";
    public static final String CUSTOM_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
    public static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ssxxx";
    public static final DateTimeFormatter ZONE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(CUSTOM_PATTERN);
    public static final DateTimeFormatter ISO_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(ISO_PATTERN);
    private static final String ERROR_UNSUPPORTED_DATE_TIME_FORMAT = "Unsupported date time format for '%s'";
    private static final List<String> LOCAL_DATE_TIME_PATTERNS = List.of("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss");
    private static final String ZONE_GMT = "GMT";
    private static final String ZONE_UTC_ABBR = "UT";
    private static final String MILLIS_READABLE_FMT = "%d min, %d sec, %d ms";
    private static final String STANDARDIZED_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final List<DateTimeFormatter> ZONED_DATE_TIME_FORMATTERS = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
            DateTimeFormatter.ofPattern(DateTimeUtil.STANDARDIZED_PATTERN),
            DateTimeFormatter.ofPattern(DateTimeUtil.CUSTOM_PATTERN),
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE,
            DateTimeFormatter.ISO_LOCAL_DATE);
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_PATTERN);
    private static final int ACCEPTED_DATE_TIME_LENGTH = 19;
    private static final Pattern ZONE_OFFSET_PATTERN = Pattern.compile("(UTC|GMT|UT).*");

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

    public static OffsetDateTime parseOffsetDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }

        //Try parse against each accepted pattern then end early on successful parse
        OffsetDateTime offsetDateTime = null;
        for (DateTimeFormatter dtf : ZONED_DATE_TIME_FORMATTERS) {
            try {
                offsetDateTime = OffsetDateTime.parse(dateTimeStr, dtf);
                break;
            } catch (DateTimeParseException e) {
                //continue to next format
            }
        }
        return offsetDateTime;
    }

    public static ZonedDateTime parseZonedDateTime(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }

        //Try parse against each accepted pattern then end early on successful parse
        ZonedDateTime zonedDateTime = null;
        for (DateTimeFormatter formatter : ZONED_DATE_TIME_FORMATTERS) {
            try {
                zonedDateTime = ZonedDateTime.parse(timeStr, formatter);
                break;
            } catch (DateTimeParseException e) {
                //continue to next format
            }
        }
        if (zonedDateTime == null) {
            throw new DateTimeException(String.format(ERROR_UNSUPPORTED_DATE_TIME_FORMAT, timeStr));
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

    public static String toCustomDateTimeFormat(String datetimeString, DateTimeFormatter customFormat) {
        ZonedDateTime zonedDateTime = parseZonedDateTime(datetimeString);
        if (zonedDateTime == null) return null;
        return zonedDateTime.format(customFormat);
    }

    /**
     * @param datetimeString sample (2023-05-23 00:00:00 GMT-07:00, 2023-05-23T00:00:00-07:00)
     *
     * @return 2023-05-23T00:00:00-07:00
     */
    public static String toIsoDateTimeFormat(String datetimeString) {
        return toCustomDateTimeFormat(datetimeString, DateTimeUtil.ISO_DATE_TIME_FORMAT);
    }

    public static OffsetDateTime toFormattedOffsetDateTime(String dateTime) {
        ZonedDateTime zonedDateTime = parseZonedDateTime(dateTime);
        if (zonedDateTime == null) {
            return null;
        }
        String formattedDateTime = DateTimeUtil.ISO_FORMATTER.format(zonedDateTime);
        return OffsetDateTime.parse(formattedDateTime, DateTimeUtil.ISO_FORMATTER);
    }

    public static LocalDateTime convertStringToLocalDateTime(String omDateTimeRawStr) {
        if (omDateTimeRawStr == null) {
            return null;
        }
        String omDateTimeStr = omDateTimeRawStr.substring(0, ACCEPTED_DATE_TIME_LENGTH);
        return DateTimeUtil.parseLocalDateTime(omDateTimeStr);
    }

    public static ZonedDateTime stringToZonedDateTime(String dateTimeStr, String timeZone) {
        if (StringUtils.isBlank(timeZone) || dateTimeStr == null) {
            return null;
        }
        LocalDateTime localeDateTime = convertStringToLocalDateTime(dateTimeStr);
        return localeDateTimeToZoneDateTime(localeDateTime, timeZone);
    }

    public static ZoneId getOffsetFromTimezoneInformation(String timezone) {
        if (timezone == null) return null;
        Matcher matcher = ZONE_OFFSET_PATTERN.matcher(timezone);
        if (matcher.find()) {
            return parseZoneId(matcher.group(0));
        }
        return null;
    }

    /**
     * @param dateString     - sample 2023-09-06 20:00:00 +0000
     * @param targetTimezone - sample UTC+08:00
     *
     * @return - 2023-09-07T04:00:00+08:00[UTC+08:00]
     */
    public static ZonedDateTime convertToTargetZoneDateTime(String dateString, String targetTimezone) {
        ZonedDateTime parsedZoneDateTime = parseZonedDateTime(dateString);
        if (parsedZoneDateTime == null || StringUtils.isBlank(targetTimezone)) {
            return parsedZoneDateTime;
        }
        try {
            return parsedZoneDateTime.withZoneSameInstant(ZoneId.of(targetTimezone));
        } catch (Exception e) {
            return parsedZoneDateTime;
        }
    }

    public static String getOffset(String date, String timezone) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(timezone)) return null;
        List<String> timezoneDetails = List.of(timezone.split(UTC));
        String offset = getTimeZoneOffset(date, CollectionUtils.firstElement(timezoneDetails));
        if (offset != null) return offset;
        String utc = CollectionUtils.lastElement(timezoneDetails);
        return StringUtils.isNotBlank(utc) ? utc : timezone;
    }
}
