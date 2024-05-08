package com.quincus.authentication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class User {
    private String id;
    private String fullName;
    @JsonProperty("username")
    private String userName;
    private String email;
    @JsonProperty("organisation_id")
    private String organizationId;
    @JsonProperty("organisation_name")
    private String organizationName;
    @JsonProperty("roles")
    @JsonAlias({"roles_details"})
    private List<Role> roles;
}
