package com.quincus.authentication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class Role {
    @JsonSetter("id")
    @JsonAlias("role_id")
    private String id;
    @JsonSetter("name")
    @JsonAlias("role_name")
    private String name;
}
