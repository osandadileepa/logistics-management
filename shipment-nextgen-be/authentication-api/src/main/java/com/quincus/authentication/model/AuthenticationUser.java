package com.quincus.authentication.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationUser {
    private String message;
    private String error;
    private String token;
    private boolean valid;
    private boolean allowed;
    private User user;

    public AuthenticationUser(String message) {
        this.message = message;
    }
}
