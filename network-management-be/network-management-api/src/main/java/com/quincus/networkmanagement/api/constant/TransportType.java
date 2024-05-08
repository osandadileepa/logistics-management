package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TransportType {
    AIR("air"),
    GROUND("ground");

    private static final Map<String, TransportType> TRANSPORT_TYPE_MAP = Stream.of(values())
            .flatMap(type -> type.names.stream().map(name -> Map.entry(name, type)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    TransportType(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static TransportType fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(TRANSPORT_TYPE_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, TransportType.class));
    }
}