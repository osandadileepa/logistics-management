package com.quincus.shipment.kafka.producers.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class ProducerUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static String localToUTC() {
        return LocalDateTime.now(Clock.systemUTC()).format(DATE_TIME_FORMATTER);
    }
}
