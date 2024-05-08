package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeUnit {
    MINUTES("m", "min", "minute", "minutes"),
    HOURS("h", "hr", "hrs", "hour", "hours"),
    SECONDS("s", "sec", "second", "seconds"),
    DAYS("d", "day", "days");

    private static final Map<String, TimeUnit> TIME_UNIT_MAP = Stream.of(values())
            .flatMap(unit -> unit.names.stream().map(name -> Map.entry(name, unit)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    TimeUnit(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static TimeUnit fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(TIME_UNIT_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, TimeUnit.class));
    }
}