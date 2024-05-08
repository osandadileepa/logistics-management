package com.quincus.web.model.error;


import lombok.Getter;

import java.util.Optional;

@Getter
public class QuincusFieldError {
    private final String field;
    private final String message;
    private final String code;
    private String rejectedValue;

    public QuincusFieldError(String field, String message, String code, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        Optional.ofNullable(rejectedValue).ifPresent(value -> this.rejectedValue = value.toString());
    }
}
