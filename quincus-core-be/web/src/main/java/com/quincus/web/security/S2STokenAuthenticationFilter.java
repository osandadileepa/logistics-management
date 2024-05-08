package com.quincus.web.security;

import com.quincus.core.impl.exception.QuincusException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class S2STokenAuthenticationFilter extends OncePerRequestFilter {

    public static final String DEFAULT_PREAUTHENTICATED_USER = "S2S";
    private static final String ERROR_AUTHENTICATING_S2S_TOKEN = "Failed to authenticate the S2S token due to error ";
    private final AuthenticationProvider authenticationProvider;
    private final TokenResolver tokenResolver;
    private final String configuredS2SToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String organizationId = tokenResolver.resolveOrganizationId(request);
        final String s2sToken = tokenResolver.resolveS2SToken(request);
        try {
            if (isAuthenticationNotSet() && StringUtils.isNotBlank(organizationId) && StringUtils.equals(s2sToken, configuredS2SToken)) {
                Authentication authentication = authenticationProvider.authenticatePreAuthenticatedUser(organizationId, DEFAULT_PREAUTHENTICATED_USER);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error(ERROR_AUTHENTICATING_S2S_TOKEN + " `{}` ", e.getMessage(), e);
            throw new QuincusException(ERROR_AUTHENTICATING_S2S_TOKEN, e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticationNotSet() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }
}