package com.quincus.qlogger.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class QLoggerResponse {
    private HttpStatus status;
    private String message;
    private JsonNode body;
    private List<String> errors;
}
