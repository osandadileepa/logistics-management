package com.quincus.core.impl.exception;

public class InvalidEnumValueException extends QuincusException {
    private static final String INVALID_ENUM_VALUE = "Invalid value '%s' for enum '%s'.";

    public InvalidEnumValueException(String enumValue, Class<? extends Enum<?>> enumClass) {
        super(String.format(INVALID_ENUM_VALUE, enumValue, enumClass.getSimpleName()));
    }

    public InvalidEnumValueException(int enumValue, Class<? extends Enum<?>> enumClass) {
        super(String.format(INVALID_ENUM_VALUE, enumValue, enumClass.getSimpleName()));
    }
}
