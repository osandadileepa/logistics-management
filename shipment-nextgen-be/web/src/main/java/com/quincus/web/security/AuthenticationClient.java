package com.quincus.web.security;

import com.quincus.authentication.api.AuthenticationApi;
import com.quincus.authentication.model.AuthenticationUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AuthenticationClient {
    private static final String TOKEN_VALIDATION_FAILED = "Token validation failed";
    private static final String LOGIN_FAILED = "Login failed for user: '%s'";
    private final AuthenticationApi authenticationApi;

    public AuthenticationUser validateToken(String token) {
        try {
            return authenticationApi.validateToken(token);
        } catch (Exception e) {
            String errorMessage = TOKEN_VALIDATION_FAILED;
            log.error(errorMessage, e);
            throw new InternalAuthenticationServiceException(errorMessage, e);
        }
    }

    public AuthenticationUser login(String userName, String password, String organizationId) {
        try {
            return authenticationApi.login(userName, password, organizationId);
        } catch (Exception e) {
            String errorMessage = String.format(LOGIN_FAILED, userName);
            log.error(errorMessage, e);
            throw new InternalAuthenticationServiceException(errorMessage, e);
        }
    }
}
