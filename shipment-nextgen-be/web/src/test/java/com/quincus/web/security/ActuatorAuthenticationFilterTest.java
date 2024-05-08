package com.quincus.web.security;

import com.quincus.web.common.exception.model.QuincusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorAuthenticationFilterTest {

    @Mock
    private AuthenticationProvider authenticationProvider;
    @Mock
    private TokenResolver tokenResolver;
    @InjectMocks
    private ActuatorAuthenticationFilter filter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness"})
    void testDoFilterInternalWhenActuatorHealth_withActuatorEndpointAndMissingToken(String endpoint) throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(endpoint);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withActuatorEndpointAndMissingToken() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/actuator/env");
        when(tokenResolver.resolveToken(request)).thenReturn(null);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(QuincusException.class)
                .hasMessage("Missing authentication token for actuator endpoint.");

        verifyNoInteractions(authenticationProvider);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withActuatorEndpointAndValidToken() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/actuator/env");
        when(tokenResolver.resolveToken(request)).thenReturn("valid-token");
        Authentication authentication = mock(Authentication.class);
        when(authenticationProvider.authenticateUser(anyString(), anyString())).thenReturn(authentication);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

}
