package com.quincus.networkmanagement.impl.web.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.networkmanagement.api.constant.NetworkManagementErrorCode;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class NetworkManagementError {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private String message;
    private NetworkManagementErrorCode code;
    private String timestamp = LocalDateTime.now(Clock.systemUTC()).format(TIMESTAMP_FORMAT);
    private List<NetworkManagementFieldError> fieldErrors;
}
