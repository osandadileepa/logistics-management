package com.quincus.networkmanagement.api.constant;

import com.quincus.networkmanagement.api.exception.InvalidEnumValueException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum AttachmentType {
    NODES("nodes"),
    CONNECTIONS("connections");

    private static final Map<String, AttachmentType> ATTACHMENT_TYPE_MAP =
            Arrays.stream(AttachmentType.values()).collect(Collectors.toMap(AttachmentType::getValue, type -> type));

    @Getter
    private final String value;

    AttachmentType(String value) {
        this.value = value;
    }

    public static AttachmentType fromValue(String enumValue) {
        return Optional.ofNullable(ATTACHMENT_TYPE_MAP.get(enumValue))
                .orElseThrow(() -> new InvalidEnumValueException(enumValue, AttachmentType.class));
    }
}
