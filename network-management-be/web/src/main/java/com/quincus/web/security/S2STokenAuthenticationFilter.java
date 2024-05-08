package com.quincus.web.security;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class S2STokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String ERROR_AUTHENTICATING_S2S_TOKEN = "Failed to authenticate the S2S token due to error ";
    private static final String S2S = "S2S";
    private final AuthenticationProvider authenticationProvider;
    private final TokenResolver tokenResolver;
    private final List<SecurityProperties.Token> configuredS2STokens;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String organizationId = tokenResolver.resolveOrganizationId(request);
        final String s2sToken = tokenResolver.resolveS2SToken(request);
        try {
            Optional<SecurityProperties.Token> token = getToken(s2sToken, configuredS2STokens);
            if (isAuthenticationNotSet() && StringUtils.isNotBlank(organizationId) && token.isPresent()) {
                Authentication authentication = authenticationProvider.authenticatePreAuthenticatedUser(organizationId, S2S, token.get().getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error(ERROR_AUTHENTICATING_S2S_TOKEN + " `{}` ", e.getMessage(), e);
            throw new QuincusException(ERROR_AUTHENTICATING_S2S_TOKEN, e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }

    private Optional<SecurityProperties.Token> getToken(String s2sToken, List<SecurityProperties.Token> configuredS2STokens) {
        if (CollectionUtils.isEmpty(configuredS2STokens)) return Optional.empty();
        return configuredS2STokens.stream().filter(token -> token.getValue().equals(s2sToken)).findFirst();
    }

    private boolean isAuthenticationNotSet() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }
}