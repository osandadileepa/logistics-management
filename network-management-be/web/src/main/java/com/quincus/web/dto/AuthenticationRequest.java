package com.quincus.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationRequest {
    private String token;
    private String username;
    private String password;
    private String organizationId;
}
