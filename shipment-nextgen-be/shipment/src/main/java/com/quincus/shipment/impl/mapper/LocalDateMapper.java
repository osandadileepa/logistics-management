package com.quincus.shipment.impl.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class LocalDateMapper {

    private static final ZoneOffset UTC_ZONE_OFFSET = ZoneOffset.UTC;

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, UTC_ZONE_OFFSET);
    }
}
