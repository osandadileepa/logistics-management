package com.quincus.web.dto;

import com.quincus.ext.annotation.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class AuthenticationRequest {
    @Size(max = 2000)
    private String token;
    @Size(max = 255)
    private String username;
    @Size(max = 255)
    private String password;
    @UUID(required = false)
    private String organizationId;
}
