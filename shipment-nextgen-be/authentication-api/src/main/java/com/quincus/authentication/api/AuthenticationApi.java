package com.quincus.authentication.api;

import com.quincus.authentication.model.AuthenticationUser;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;

public interface AuthenticationApi {

    AuthenticationUser login(@NotBlank String userName, @NotBlank String password, @Nullable String organizationId);

    AuthenticationUser validateToken(@NotBlank String token);
}
