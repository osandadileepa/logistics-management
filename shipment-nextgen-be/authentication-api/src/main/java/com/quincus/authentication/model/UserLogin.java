package com.quincus.authentication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserLogin {
    private String id;
    private String firstName;
    private String lastName;
    private String message;
    private String token;
    @JsonProperty("error")
    @JsonAlias("errors")
    private String error;
    private List<Role> roles;
}
