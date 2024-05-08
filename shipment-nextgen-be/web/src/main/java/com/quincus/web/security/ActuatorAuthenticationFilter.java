package com.quincus.web.security;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class ActuatorAuthenticationFilter extends OncePerRequestFilter {
    private static final String ACTUATOR_URL = "/actuator";
    private final AuthenticationProvider authenticationProvider;
    private final TokenResolver tokenResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        //Skip authentication for actuator-health related endpoints
        if (requestURI.startsWith(ACTUATOR_URL + "/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().startsWith(ACTUATOR_URL)) {
            final String token = tokenResolver.resolveToken(request);
            if (StringUtils.isBlank(token)) {
                handleUnauthorizedError("Missing authentication token for actuator endpoint.");
                return;
            }
            try {
                final Authentication authentication = authenticationProvider.authenticateUser(token, request.getRequestURI());
                if (authentication == null) {
                    handleUnauthorizedError("Failed to authenticate for actuator endpoint.");
                }
            } catch (Exception e) {
                handleUnauthorizedError("Error occurred during authentication due to invalid credentials or other issues.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleUnauthorizedError(String errorMessage) {
        log.error(errorMessage);
        throw new QuincusException(errorMessage);
    }
}
