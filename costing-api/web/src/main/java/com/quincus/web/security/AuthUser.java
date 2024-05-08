package com.quincus.web.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthUser {

    private String username;
    private String [] authorities;

}
