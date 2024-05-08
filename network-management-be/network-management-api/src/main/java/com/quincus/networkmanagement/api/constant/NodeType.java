package com.quincus.networkmanagement.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NodeType {
    BAGGAGE_CLAIM("baggage_claim", "baggage claim"),
    CARGO("cargo"),
    SMALL_PACKAGE_COUNTER("small_package_counter", "small package counter"),
    TICKET_COUNTER("ticket_counter", "ticket counter"),
    OTHERS("other", "others");

    private static final Map<String, NodeType> NODE_TYPE_MAP = Stream.of(values())
            .flatMap(unit -> unit.names.stream().map(name -> Map.entry(name, unit)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Set<String> names;

    NodeType(String... names) {
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    @JsonCreator
    public static NodeType fromValue(String stringValue) {
        if (stringValue == null) return null;
        return Optional.ofNullable(NODE_TYPE_MAP.get(stringValue.toLowerCase()))
                .orElseThrow(() -> new InvalidEnumValueException(stringValue, NodeType.class));
    }
}