package com.quincus.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S2STokenAuthenticationFilterTest {

    @Mock
    private AuthenticationProvider authenticationProvider;
    @Mock
    private TokenResolver tokenResolver;

    @InjectMocks
    private S2STokenAuthenticationFilter filter;

    @BeforeEach
    public void setup() {
        String configuredS2SToken = "configuredS2SToken";
        filter = new S2STokenAuthenticationFilter(authenticationProvider, tokenResolver, configuredS2SToken);
    }

    @Test
    @DisplayName("Do filter internal - successful authentication")
    void doFilterInternal_SuccessfulAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String organizationId = "organizationId";
        String s2sToken = "S2S";
        when(tokenResolver.resolveOrganizationId(request)).thenReturn(organizationId);
        when(tokenResolver.resolveS2SToken(request)).thenReturn(s2sToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(tokenResolver).resolveOrganizationId(request);
        verify(tokenResolver).resolveS2SToken(request);
    }

    @Test
    @DisplayName("Do filter internal - authentication not set")
    void doFilterInternal_AuthenticationNotSet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(tokenResolver.resolveOrganizationId(request)).thenReturn("organizationId");
        when(tokenResolver.resolveS2SToken(request)).thenReturn("configuredS2SToken");

        Authentication authentication = mock(Authentication.class);
        when(authenticationProvider.authenticatePreAuthenticatedUser("organizationId", "S2S")).thenReturn(authentication);

        // Clear any existing authentication
        SecurityContextHolder.clearContext();

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        verify(authenticationProvider).authenticatePreAuthenticatedUser("organizationId", "S2S");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    }

    @Test
    @DisplayName("Do filter internal - authentication already set")
    void doFilterInternal_AuthenticationAlreadySet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Set a mock authentication in the SecurityContextHolder
        Authentication existingAuthentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuthentication);
    }
}
