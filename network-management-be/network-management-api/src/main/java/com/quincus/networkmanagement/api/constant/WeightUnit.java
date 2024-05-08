package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WeightUnit {
    KILOGRAMS("kg", "kilogram", "kilograms"),
    GRAMS("g", "gram", "grams"),
    POUNDS("lb", "lbs", "pound", "pounds"),
    OUNCE("oz", "ounce", "ounces");

    private static final Map<String, WeightUnit> WEIGHT_UNIT_MAP = Stream.of(values())
            .flatMap(unit -> unit.names.stream().map(name -> Map.entry(name, unit)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    WeightUnit(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static WeightUnit fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(WEIGHT_UNIT_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, WeightUnit.class));
    }
}