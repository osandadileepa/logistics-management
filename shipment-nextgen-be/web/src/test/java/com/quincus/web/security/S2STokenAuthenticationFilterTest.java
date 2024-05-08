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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        SecurityProperties.Token token = new SecurityProperties.Token();
        token.setName("Token Name");
        token.setValue("Token Value");
        List<SecurityProperties.Token> configuredS2SToken = List.of(token);
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

        // Clear any existing authentication
        SecurityContextHolder.clearContext();

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
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

    @Test
    void testDoFilterInternal_withInvalidToken_doesNotAuthenticateUser() throws ServletException, IOException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(tokenResolver.resolveOrganizationId(request)).thenReturn("orgId");
        when(tokenResolver.resolveS2SToken(request)).thenReturn("INVALID_TOKEN");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenResolver).resolveOrganizationId(request);
        verify(tokenResolver).resolveS2SToken(request);
        verifyNoInteractions(authenticationProvider);
        verify(filterChain).doFilter(request, response);
    }
}
