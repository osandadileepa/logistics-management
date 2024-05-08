package com.quincus.web.controller;

import com.quincus.authentication.model.AuthenticationUser;
import com.quincus.web.SessionController;
import com.quincus.web.dto.AuthenticationRequest;
import com.quincus.web.dto.AuthenticationResponse;
import com.quincus.web.security.AuthenticationClient;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/sessions")
public class SessionControllerImpl implements SessionController {
    private static final String INVALID_AUTHENTICATION_PARAMETERS_PROVIDED = "Invalid authentication parameters provided.";
    private final AuthenticationClient authenticationClient;

    public AuthenticationResponse<AuthenticationUser> create(final AuthenticationRequest authenticationRequest) {
        if (StringUtils.isNotBlank(authenticationRequest.getToken())) {
            return new AuthenticationResponse<>(authenticationClient.validateToken(authenticationRequest.getToken()));
        }
        if (StringUtils.isNotBlank(authenticationRequest.getUsername()) && StringUtils.isNotBlank(authenticationRequest.getPassword())) {
            return new AuthenticationResponse<>(authenticationClient.login(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword(), authenticationRequest.getOrganizationId()));
        }
        throw new BadCredentialsException(INVALID_AUTHENTICATION_PARAMETERS_PROVIDED);
    }

}