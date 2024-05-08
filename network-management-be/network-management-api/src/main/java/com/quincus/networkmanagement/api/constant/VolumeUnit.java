package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VolumeUnit {
    CUBIC_METERS("m3", "cu m", "cubic meter", "cubic meters", "cubic_meter", "cubic_meters"),
    CUBIC_FEET("ft3", "cu ft", "cubic foot", "cubic feet", "cubic_foot", "cubic_feet");

    private static final Map<String, VolumeUnit> VOLUME_UNIT_MAP = Stream.of(values())
            .flatMap(unit -> unit.names.stream().map(name -> Map.entry(name, unit)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    VolumeUnit(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static VolumeUnit fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(VOLUME_UNIT_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, VolumeUnit.class));
    }
}