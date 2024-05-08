package com.quincus.web.common.exception.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@NoArgsConstructor
public class QuincusError {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private String message;
    private String code;
    private String errorReferenceId = UUID.randomUUID().toString();
    private String timestamp = LocalDateTime.now(Clock.systemUTC()).format(TIMESTAMP_FORMAT);
    private List<QuincusFieldError> fieldErrors;
}