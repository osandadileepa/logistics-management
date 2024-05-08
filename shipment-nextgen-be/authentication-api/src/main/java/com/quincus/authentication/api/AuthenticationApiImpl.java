package com.quincus.authentication.api;

import com.quincus.authentication.model.AuthenticationUser;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Service
public class AuthenticationApiImpl implements AuthenticationApi {
    private final AuthenticationRestService authenticationRestService;

    @Override
    public AuthenticationUser login(@NotBlank String userName, @NotBlank String password, @Nullable String organizationId) {
        return authenticationRestService.login(userName, password, organizationId);
    }

    @Override
    public AuthenticationUser validateToken(@NotBlank String token) {
        return authenticationRestService.validateToken(token);
    }
}
