package com.quincus.authentication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class TokenValidation {
    private String message;
    @JsonProperty("error")
    @JsonAlias("errors")
    private String error;
    @JsonSetter("is_valid")
    private boolean valid;
    @JsonSetter("is_allowed")
    private boolean allowed;
    private User user;
}
