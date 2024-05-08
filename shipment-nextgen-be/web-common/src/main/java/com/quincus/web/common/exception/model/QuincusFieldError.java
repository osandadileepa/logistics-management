package com.quincus.web.common.exception.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuincusFieldError {
    private String field;
    private String message;
    private String code;
    private String rejectedValue;

    public QuincusFieldError(String field, String message, String code, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.code = code;
        if (rejectedValue != null) {
            this.rejectedValue = rejectedValue.toString();
        }
    }
}
