package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DimensionUnit {
    MILLIMETERS("mm", "millimeter", "millimeters"),
    INCHES("in", "inch", "inches"),
    METERS("m", "meter", "meters"),
    FEET("ft", "foot", "feet");

    private static final Map<String, DimensionUnit> DIMENSION_UNIT_MAP = Stream.of(values())
            .flatMap(unit -> unit.names.stream().map(name -> Map.entry(name, unit)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    DimensionUnit(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static DimensionUnit fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(DIMENSION_UNIT_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, DimensionUnit.class));
    }
}
