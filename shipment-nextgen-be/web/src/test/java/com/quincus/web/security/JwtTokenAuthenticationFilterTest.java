package com.quincus.web.security;

import com.quincus.web.common.exception.model.QuincusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationFilterTest {

    @Mock
    private AuthenticationProvider authenticationProvider;
    @Mock
    private TokenResolver tokenResolver;
    @InjectMocks
    private JwtTokenAuthenticationFilter filter;

    @Test
    @DisplayName("Do filter internal - successful authentication")
    void doFilterInternal_SuccessfulAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String token = "validToken";

        when(tokenResolver.resolveToken(request)).thenReturn(token);

        Authentication authentication = mock(Authentication.class);
        when(authenticationProvider.authenticateUser(token, request.getRequestURI())).thenReturn(authentication);

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationProvider).authenticateUser(token, request.getRequestURI());
        verify(filterChain).doFilter(request, response);
    }


    @Test
    @DisplayName("Do filter internal - no token")
    void doFilterInternal_NoToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(tokenResolver.resolveToken(request)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(tokenResolver).resolveToken(request);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Do filter internal - other exception")
    void doFilterInternal_OtherException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String token = "validToken";

        when(tokenResolver.resolveToken(request)).thenReturn(token);
        when(authenticationProvider.authenticateUser(token, request.getRequestURI())).thenThrow(RuntimeException.class);

        QuincusException expectedException = org.junit.jupiter.api.Assertions.assertThrows(QuincusException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        verify(authenticationProvider).authenticateUser(token, request.getRequestURI());
        assertThat(expectedException.getMessage()).isEqualTo("Error occurred when authenticating jwt token due to bad credentials or unknown error.");
    }

}