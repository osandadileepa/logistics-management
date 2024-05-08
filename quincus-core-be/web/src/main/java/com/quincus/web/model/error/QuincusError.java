package com.quincus.web.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class QuincusError {
    private String errorMessage;
    private String errorCode;
    private String errorReferenceId = UUID.randomUUID().toString();
    private String timestamp = LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_LOCAL_DATE);
    private List<QuincusFieldError> fieldErrors;
}
