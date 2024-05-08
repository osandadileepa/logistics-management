package com.quincus.ext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DateTimeUtilTest {

    private static Stream<Arguments> provideValidDateTime() {
        return Stream.of(
                Arguments.of("2022-12-29 01:16:03", 2022, 12, 29, 1, 16, 3),
                Arguments.of("2023-01-01 00:00:00", 2023, 1, 1, 0, 0, 0),
                Arguments.of("2022-01-30 12:22:39", 2022, 1, 30, 12, 22, 39),
                Arguments.of("2022-12-29 01:16:03", 2022, 12, 29, 1, 16, 3),
                Arguments.of("2022-01-30 12:22:39", 2022, 1, 30, 12, 22, 39),
                Arguments.of("2023-01-01 00:00:00", 2023, 1, 1, 0, 0, 0)
        );
    }

    private static Stream<Arguments> provideValidDateTimeAndTimeZone() {
        return Stream.of(
                Arguments.of(2022, 12, 29, 1, 16, 3, "GMT+08:00"),
                Arguments.of(2023, 1, 1, 0, 0, 0, "GMT-05:00"),
                Arguments.of(2022, 1, 30, 12, 22, 39, "GMT+07:00"),
                Arguments.of(2022, 12, 29, 1, 16, 3, "GMT+10:00"),
                Arguments.of(2022, 1, 30, 12, 22, 39, "GMT-04:00"),
                Arguments.of(2023, 1, 1, 0, 0, 0, "GMT-3")
        );
    }

    private static Stream<Arguments> provideValidZoneOffset() {
        return Stream.of(
                Arguments.of("2022-12-29 01:16:03 GMT+08:00", 2022, 12, 29, 1, 16, 3, ZoneId.of("GMT+08:00")),
                Arguments.of("2023-01-01 00:00:00 UTC+00:00", 2023, 1, 1, 0, 0, 0, ZoneId.of("UTC+00:00")),
                Arguments.of("2022-01-30 12:22:39 GMT-3", 2022, 1, 30, 12, 22, 39, ZoneId.of("GMT-3")),
                Arguments.of("2022-12-29 01:16:03 +0000", 2022, 12, 29, 1, 16, 3, ZoneId.of("+0000")),
                Arguments.of("2022-01-30 12:22:39 -0800", 2022, 1, 30, 12, 22, 39, ZoneId.of("-0800")),
                Arguments.of("2023-01-01 00:00:00 -08:00", 2023, 1, 1, 0, 0, 0, ZoneId.of("-08:00"))
        );
    }

    private static Stream<Arguments> provideValidTimezone() {
        return Stream.of(
                Arguments.of("GMT+08:00", ZoneId.ofOffset("GMT", ZoneOffset.of("+08:00"))),
                Arguments.of("GMT-04:00", ZoneId.ofOffset("GMT", ZoneOffset.of("-04:00"))),
                Arguments.of("UTC+08:00", ZoneId.ofOffset("UTC", ZoneOffset.of("+08:00"))),
                Arguments.of("UTC-04:00", ZoneId.ofOffset("UTC", ZoneOffset.of("-04:00"))),
                Arguments.of("GMT +08:00", ZoneId.ofOffset("GMT", ZoneOffset.of("+08:00"))),
                Arguments.of("GMT -04:00", ZoneId.ofOffset("GMT", ZoneOffset.of("-04:00"))),
                Arguments.of("UTC +08:00", ZoneId.ofOffset("UTC", ZoneOffset.of("+08:00"))),
                Arguments.of("UTC -04:00", ZoneId.ofOffset("UTC", ZoneOffset.of("-04:00"))),
                Arguments.of("GMT+8", ZoneId.ofOffset("GMT", ZoneOffset.of("+08:00"))),
                Arguments.of("GMT-4", ZoneId.ofOffset("GMT", ZoneOffset.of("-04:00"))),
                Arguments.of("GMT+10", ZoneId.ofOffset("GMT", ZoneOffset.of("+10:00"))),
                Arguments.of("UTC+8", ZoneId.ofOffset("UTC", ZoneOffset.of("+08:00"))),
                Arguments.of("UTC-4", ZoneId.ofOffset("UTC", ZoneOffset.of("-04:00"))),
                Arguments.of("UTC-10", ZoneId.ofOffset("UTC", ZoneOffset.of("-10:00"))),
                Arguments.of("UTC", ZoneId.ofOffset("UTC", ZoneOffset.UTC)),
                Arguments.of("EST", ZoneId.of("EST", ZoneId.SHORT_IDS)),
                Arguments.of("MST", ZoneId.of("MST", ZoneId.SHORT_IDS)),
                Arguments.of("PST", ZoneId.of("PST", ZoneId.SHORT_IDS))
        );
    }

    private static Stream<Arguments> provideLocalDateTimeWithTimezone() {
        LocalDateTime dateTimeNow = LocalDateTime.now();
        LocalDateTime dstDateTime = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0, 0);
        LocalDateTime nonDstDateTime = LocalDateTime.of(2023, Month.NOVEMBER, 6, 0, 0, 0);
        return Stream.of(
                Arguments.of("UTC", dateTimeNow, "UTC"),
                Arguments.of("UTC+08:00", dateTimeNow, "UTC+08:00"),
                Arguments.of("UT-08:00", dateTimeNow, "UTC-08:00"),
                Arguments.of("GMT", dateTimeNow, "UTC"),
                Arguments.of("GMT+08:00", dateTimeNow, "UTC+08:00"),
                Arguments.of("EST", dateTimeNow, "UTC-05:00"),
                Arguments.of("MST", dateTimeNow, "UTC-07:00"),
                Arguments.of("HST", dateTimeNow, "UTC-10:00"),
                Arguments.of("IST", dateTimeNow, "UTC+05:30"),
                Arguments.of("PST", dstDateTime, "UTC-07:00"),
                Arguments.of("PST", nonDstDateTime, "UTC-08:00"),
                Arguments.of("ECT", dstDateTime, "UTC+02:00"),
                Arguments.of("ECT", nonDstDateTime, "UTC+01:00")
        );
    }

    private static Stream<Arguments> provideMillisecondsAndExpectedFormat() {
        return Stream.of(
                Arguments.of(0, "0 min, 0 sec, 0 ms"),
                Arguments.of(100, "0 min, 0 sec, 100 ms"),
                Arguments.of(1000, "0 min, 1 sec, 0 ms"),
                Arguments.of(100000, "1 min, 40 sec, 0 ms"),
                Arguments.of(100001, "1 min, 40 sec, 1 ms")
        );
    }

    private static Stream<Arguments> provideValidISODateTime() {
        return Stream.of(
                Arguments.of("2023-05-15T01:47:38.060Z", "2023-05-15 01:47:38 +0000"),
                Arguments.of("2023-06-11T14:52:38.966Z", "2023-06-11 14:52:38 +0000"),
                Arguments.of("2023-07-12 15:27:02 +0800", "2023-07-12 15:27:02 +0800"),
                Arguments.of("2022-11-29T12:54:00.000-06:00", "2022-11-29 12:54:00 -0600"),
                Arguments.of("", null),
                Arguments.of("   ", null),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> providedValidStringDatesAndExpectedResult() {
        return Stream.of(
                Arguments.of("2023-05-15T01:47:38.060Z", "2023-05-15T01:47:38+00:00"),
                Arguments.of("2023-06-11T14:52:38.966Z", "2023-06-11T14:52:38+00:00"),
                Arguments.of("2023-07-12 15:27:02 +0800", "2023-07-12T15:27:02+08:00"),
                Arguments.of("2023-07-12T15:27:02+08:00", "2023-07-12T15:27:02+08:00"),
                Arguments.of("2022-11-29T12:54:00.000-06:00", "2022-11-29T12:54:00-06:00"),
                Arguments.of("2023-05-23 00:00:00 GMT-07:00", "2023-05-23T00:00:00-07:00"),

                Arguments.of("", null),
                Arguments.of("   ", null),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> provideTimeZoneInformation() {
        return Stream.of(
                Arguments.of("Asia/Manila UTC+08:00", "UTC+08:00"),
                Arguments.of("Asia/Kabul UTC+04:30", "UTC+04:30"),
                Arguments.of("Europe/Kirov UTC+03:00", "UTC+03:00"),
                Arguments.of("America/Chicago UTC-05:00", "UTC-05:00")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidDateTime")
    void parseLocalDateTime_timeStrArgumentDateTime_shouldReturnLocalDateTime(String dateTimeStr, int year,
                                                                              int month, int day, int hour,
                                                                              int minute, int second) {
        LocalDateTime expectedDateTime = LocalDateTime.of(year, month, day, hour, minute, second, 0);
        LocalDateTime resultDateTime = DateTimeUtil.parseLocalDateTime(dateTimeStr);

        assertThat(resultDateTime).isNotNull();

        var expectedEpoch = expectedDateTime.toEpochSecond(ZoneOffset.UTC);
        var actualEpoch = resultDateTime.toEpochSecond(ZoneOffset.UTC);
        assertThat(actualEpoch).withFailMessage("String input was not parsed correctly.").isEqualTo(expectedEpoch);
    }

    @Test
    void parseLocalDateTime_timeStrArgumentNull_shouldReturnNull() {
        assertThat(DateTimeUtil.parseLocalDateTime(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideValidZoneOffset")
    void parseZonedDateTime_timeStrArgumentDateTimeZoneOffset_shouldReturnZonedDateTime(String dateTimeStr, int year,
                                                                                        int month, int day, int hour,
                                                                                        int minute, int second,
                                                                                        ZoneId zoneId) {
        ZonedDateTime expectedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, zoneId);
        ZonedDateTime resultDateTime = DateTimeUtil.parseZonedDateTime(dateTimeStr);

        assertThat(resultDateTime).isNotNull();

        var expectedEpoch = expectedDateTime.toEpochSecond();
        var actualEpoch = resultDateTime.toEpochSecond();
        assertThat(actualEpoch).withFailMessage("String input was not parsed correctly.").isEqualTo(expectedEpoch);
    }

    @Test
    void parseZonedDateTime_timeStrArgumentNull_shouldReturnNull() {
        assertThat(DateTimeUtil.parseZonedDateTime(null)).isNull();
    }

    @Test
    void parseInstant_timeStrArgumentInstant_shouldReturnInstant() {
        String instantStr = "2022-12-11T14:52:38.966Z";
        Instant resultInstant = DateTimeUtil.parseInstant(instantStr);

        assertThat(resultInstant.toString()).withFailMessage("String input was not parsed correctly.").hasToString(instantStr);
    }

    @Test
    void parseInstant_timeStrArgumentNull_shouldReturnNull() {
        assertThat(DateTimeUtil.parseInstant(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideValidTimezone")
    void parseZoneId_timezoneStrArgument_shouldReturnZoneId(String timezoneStr, ZoneId expectedZoneId) {
        ZoneId resultZoneId = DateTimeUtil.parseZoneId(timezoneStr);

        assertThat(resultZoneId).withFailMessage("Zone ID was not parsed correctly.").isEqualTo(expectedZoneId);
    }

    @ParameterizedTest
    @MethodSource("provideLocalDateTimeWithTimezone")
    void convertTimezoneToUtc_timezoneStrArgument_shouldReturnUtcTimezone(String timezone, LocalDateTime refDateTime, String expectedUtcTimezone) {
        String actualUtcTimezone = DateTimeUtil.convertTimezoneToUtc(timezone, refDateTime);
        assertThat(actualUtcTimezone).isEqualTo(expectedUtcTimezone);
    }

    @Test
    void parseZoneId_timezoneStrArgumentNull_shouldReturnNull() {
        assertThat(DateTimeUtil.parseZoneId(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideMillisecondsAndExpectedFormat")
    void convertMillisToReadableFormat_positiveInteger_shouldReturnTimeFormat(long millisecond, String expectedFmt) {
        assertThat(DateTimeUtil.formatDuration(millisecond)).isEqualTo(expectedFmt);
    }

    @Test
    void convertMillisToReadableFormat_negativeInteger_shouldReturnTimeFormat() {
        assertThatThrownBy(() -> DateTimeUtil.formatDuration(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "2023-05-23T00:00:00Z, true",  // Past date
            "2022-01-01T00:00:00Z, true",  // Past date
    })
    void testIsTodayOrPastDate_PastDate_ReturnsTrue(String dateStr, boolean expected) {
        assertThat(DateTimeUtil.isTodayOrPastDate(dateStr)).isEqualTo(expected);
    }

    @Test
    void testIsTodayOrPastDate_FutureDatePlusOneDay_ReturnsFalse() {
        OffsetDateTime futureDatePlusOneDay = OffsetDateTime.now().plusYears(1000);

        assertThat(DateTimeUtil.isTodayOrPastDate(futureDatePlusOneDay.toString())).isFalse();
    }

    @Test
    void testIsTodayOrPastDate_CurrentDate_ReturnsTrue() {
        String currentDate = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        assertThat(DateTimeUtil.isTodayOrPastDate(currentDate)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideValidDateTimeAndTimeZone")
    void testValidLocaleDateTimeToZoneDateTime_TransformToCorrectData(int year, int month, int day, int hour,
                                                                      int minute, int second, String timeZone) {
        LocalDateTime actualLocaleDate = LocalDateTime.of(year, month, day, hour, minute, second, 0);

        ZonedDateTime transformedZoneDateTime = DateTimeUtil.localeDateTimeToZoneDateTime(actualLocaleDate, timeZone);
        assertThat(transformedZoneDateTime).isNotNull();
        assertThat(transformedZoneDateTime.getYear()).isEqualTo(year);
        assertThat(transformedZoneDateTime.getMonthValue()).isEqualTo(month);
        assertThat(transformedZoneDateTime.getDayOfMonth()).isEqualTo(day);
        assertThat(transformedZoneDateTime.getHour()).isEqualTo(hour);
        assertThat(transformedZoneDateTime.getMinute()).isEqualTo(minute);
        assertThat(transformedZoneDateTime.getSecond()).isEqualTo(second);
        assertThat(transformedZoneDateTime.getZone()).isEqualTo(DateTimeUtil.parseZoneId(timeZone));
    }

    @Test
    void testLocaleDateTimeToZoneDateTime_BlankZone_ReturnsNull() {
        assertThat(DateTimeUtil.localeDateTimeToZoneDateTime(LocalDateTime.now(), "")).isNull();
    }

    @Test
    void testLocaleDateTimeToZoneDateTime_InvalidTimeZone_ReturnsNullAndNotThrowException() {
        assertThat(DateTimeUtil.localeDateTimeToZoneDateTime(LocalDateTime.now(), "INVALID_ZONE")).isNull();
    }

    @Test
    void testLocaleDateTimeToZoneDateTime_NullDateTime_ReturnsNull() {
        assertThat(DateTimeUtil.localeDateTimeToZoneDateTime(null, "GMT+08:00")).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideValidISODateTime")
    void convertISOInstantToUtc_timezoneStrArgument_shouldReturnUtcTimezone(String isoDateTime, String expected) {
        assertThat(DateTimeUtil.toCustomDateTimeFormat(isoDateTime, DateTimeUtil.ZONE_DATE_TIME_FORMAT)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("providedValidStringDatesAndExpectedResult")
    void testToIsoDateTimeFormat(String isoDateTime, String expected) {
        assertThat(DateTimeUtil.toIsoDateTimeFormat(isoDateTime)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideTimeZoneInformation")
    void getOffsetFromTimezoneInformation_timezoneStrArgument_shouldReturnOffset(String timezone, ZoneId expected) {
        assertThat(DateTimeUtil.getOffsetFromTimezoneInformation(timezone)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideStringDepartureTimeAndTimeZone")
    void givenParameterizeDateAndTimeZone_whenConvertToTargetZoneDateTime_shouldHandleAllCase(String dateToConvert, String timeZone, String expectedResult) {
        assertThat(DateTimeUtil.convertToTargetZoneDateTime(dateToConvert, timeZone)).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> provideStringDepartureTimeAndTimeZone() {
        return Stream.of(
                Arguments.of("2023-09-06 20:00:00 +0000", "UTC+08:00", "2023-09-07T04:00:00+08:00[UTC+08:00]"),
                Arguments.of("2023-09-06 20:00:00 +0800", "UTC+08:00", "2023-09-06T20:00:00+08:00[UTC+08:00]"),
                Arguments.of("2023-09-06 20:00:00 +0000", "", "2023-09-06T20:00:00+00:00[UTC+00:00]"),
                Arguments.of("2023-09-06 20:00:00 +0000", "xxx", "2023-09-06T20:00:00+00:00[UTC+00:00]")
        );
    }
}
