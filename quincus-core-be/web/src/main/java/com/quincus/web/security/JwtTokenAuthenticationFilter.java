package com.quincus.web.security;

import com.quincus.core.impl.exception.ApiCallException;
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
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String ERROR_OCCURRED_WHEN_AUTHENTICATING_TOKEN_DUE_TO_BAD_CREDENTIALS_OR_UNKNOWN_ERROR = "Error occurred when authenticating jwt token due to bad credentials or unknown error.";
    private static final String ERR_Q_PORTAL_API_ERROR = "Error encounter while QPortal API.";
    private final AuthenticationProvider authenticationProvider;
    private final TokenResolver tokenResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String token = tokenResolver.resolveToken(request);
        try {
            if (StringUtils.isNotBlank(token)) {
                final Authentication authentication = authenticationProvider.authenticateUser(token, request.getRequestURI());
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ApiCallException e) {
            log.error(ERR_Q_PORTAL_API_ERROR, e);
            throw new QuincusException(ERR_Q_PORTAL_API_ERROR, e);
        } catch (Exception e) {
            log.error(ERROR_OCCURRED_WHEN_AUTHENTICATING_TOKEN_DUE_TO_BAD_CREDENTIALS_OR_UNKNOWN_ERROR, e);
            throw new QuincusException(ERROR_OCCURRED_WHEN_AUTHENTICATING_TOKEN_DUE_TO_BAD_CREDENTIALS_OR_UNKNOWN_ERROR, e);
        }
        filterChain.doFilter(request, response);
    }

}