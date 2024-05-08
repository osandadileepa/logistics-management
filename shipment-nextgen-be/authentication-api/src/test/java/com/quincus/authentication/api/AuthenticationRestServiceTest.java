package com.quincus.authentication.api;

import com.quincus.authentication.config.AuthenticationProperties;
import com.quincus.authentication.exception.AuthenticationApiException;
import com.quincus.authentication.mapper.AuthenticationUserMapper;
import com.quincus.authentication.model.TokenValidation;
import com.quincus.authentication.model.UserLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationRestServiceTest {
    private static final String ERROR_MESSAGE = "Exception occurred in User Authentication: {}";
    @InjectMocks
    AuthenticationRestService authenticationRestService;
    @Mock
    AuthenticationProperties authenticationProperties;
    @Mock
    RestTemplate restTemplate;
    @Mock
    AuthenticationUserMapper authenticationUserMapper;
    String baseUrl;
    HttpHeaders headers;
    HttpEntity<Map<String, String>> request;
    String json;

    @BeforeEach
    void before() {
        baseUrl = "https://api.auth.test.quincus.com/";
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        json = "{\"message\": \"Authentication is successful\"}";
    }

    @Test
    void loginWithOrganizationId_ShouldReturnResult() {
        String api = "api/v1/users/login.json/";
        URI uri = URI.create(baseUrl + api);
        request = new HttpEntity<>(Map.of("username", "dummyUsername", "password", "dummyPassword",
                "organisation_id", "dummyOrganizationId"), headers);
        ResponseEntity<UserLogin> response = new ResponseEntity<>(mock(UserLogin.class), HttpStatus.OK);
        when(authenticationProperties.getBaseUrl()).thenReturn(baseUrl);
        when(authenticationProperties.getLoginApi()).thenReturn(api);
        when(restTemplate.postForEntity(uri, request, UserLogin.class)).thenReturn(response);
        assertThat(authenticationRestService.login("dummyUsername", "dummyPassword", "dummyOrganizationId")).isNotNull();
    }

    @Test
    void loginWithoutOrganizationId_ShouldReturnResult() {
        String api = "api/v1/users/login.json/";
        URI uri = URI.create(baseUrl + api);
        request = new HttpEntity<>(Map.of("username", "dummyUsername", "password", "dummyPassword"), headers);
        ResponseEntity<UserLogin> response = new ResponseEntity<>(mock(UserLogin.class), HttpStatus.OK);
        when(authenticationProperties.getBaseUrl()).thenReturn(baseUrl);
        when(authenticationProperties.getLoginApi()).thenReturn(api);
        when(restTemplate.postForEntity(uri, request, UserLogin.class)).thenReturn(response);
        assertThat(authenticationRestService.login("dummyUsername", "dummyPassword", null)).isNotNull();
    }

    @Test
    void validateToken_ShouldReturnResult() {
        String api = "api/v1/users/validate_token.json/";
        URI uri = URI.create(baseUrl + api);
        request = new HttpEntity<>(Collections.singletonMap("jwt_token", "dummyToken"), headers);
        ResponseEntity<TokenValidation> response = new ResponseEntity<>(mock(TokenValidation.class), HttpStatus.OK);
        when(authenticationProperties.getBaseUrl()).thenReturn(baseUrl);
        when(authenticationProperties.getValidateTokenApi()).thenReturn(api);
        when(restTemplate.postForEntity(uri, request, TokenValidation.class)).thenReturn(response);
        assertThat(authenticationRestService.validateToken("dummyToken")).isNotNull();
    }

    @Test
    void validateToken_ValidationException_ThrowsAuthenticationApiException() {
        String api = "api/v1/users/validate_token.json/";
        URI uri = URI.create(baseUrl + api);

        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(TokenValidation.class)))
                .thenThrow(new RuntimeException("Token validation failed"));

        assertThatExceptionOfType(AuthenticationApiException.class)
                .isThrownBy(() -> authenticationRestService.validateToken("dummyToken"))
                .withMessage(String.format(ERROR_MESSAGE, "Token validation failed"));
    }

    @Test
    void login_loginException_ThrowsAuthenticationApiException() {
        String api = "api/v1/users/login.json/";
        URI uri = URI.create(baseUrl + api);

        when(restTemplate.postForEntity(eq(uri), any(HttpEntity.class), eq(TokenValidation.class)))
                .thenThrow(new RuntimeException("Token validation failed"));

        assertThatExceptionOfType(AuthenticationApiException.class)
                .isThrownBy(() -> authenticationRestService.login("dummyUsername", "dummyPassword", "dummyOrganizationId"))
                .withMessage(String.format(ERROR_MESSAGE, "Token validation failed"));
    }
}
