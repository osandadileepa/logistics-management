package com.quincus.authentication.api;

import com.quincus.authentication.config.AuthenticationProperties;
import com.quincus.authentication.mapper.AuthenticationUserMapper;
import com.quincus.authentication.model.AuthenticationUser;
import com.quincus.authentication.model.TokenValidation;
import com.quincus.authentication.model.UserLogin;
import com.quincus.core.impl.exception.ApiCallException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class AuthenticationRestService {
    public static final String ORGANIZATION_ID = "organisation_id";
    private static final String ERROR_IN_USER_AUTHENTICATION = "Exception occurred in User Authentication: {}";
    private static final String JWT_TOKEN = "jwt_token";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private final RestTemplate restTemplate;
    private final AuthenticationProperties authenticationProperties;
    private final AuthenticationUserMapper mapper = Mappers.getMapper(AuthenticationUserMapper.class);

    public AuthenticationUser login(@NotBlank String userName, @NotBlank String password, @Nullable String organizationId) {
        URI uri = getUri(authenticationProperties.getLoginApi());
        Map<String, String> map = new HashMap<>();
        map.put(USERNAME, userName);
        map.put(PASSWORD, password);
        if (StringUtils.isNotBlank(organizationId)) {
            map.put(ORGANIZATION_ID, organizationId);
        }
        HttpEntity<Map<String, String>> request = createHttpRequest(map);
        try {
            ResponseEntity<UserLogin> response = restTemplate.postForEntity(uri, request, UserLogin.class);
            return mapper.userLoginToAuthUser(response.getBody());
        } catch (Exception e) {
            log.error(ERROR_IN_USER_AUTHENTICATION, e.getMessage());
            throw new ApiCallException("Error encountered during authentication login api.", request, e.getMessage(), e);
        }
    }

    public AuthenticationUser validateToken(@NotBlank String token) {
        URI uri = getUri(authenticationProperties.getValidateTokenApi());
        HttpEntity<Map<String, String>> request = createHttpRequest(Collections.singletonMap(JWT_TOKEN, token));
        try {
            ResponseEntity<TokenValidation> response = restTemplate.postForEntity(uri, request, TokenValidation.class);
            return mapper.tokenValidationToAuthUser(response.getBody());
        } catch (Exception e) {
            log.error(ERROR_IN_USER_AUTHENTICATION, e.getMessage());
            throw new ApiCallException("Error encountered during validate token api.", request, e.getMessage(), e);
        }
    }

    private URI getUri(String api) {
        return URI.create(authenticationProperties.getBaseUrl() + api);
    }

    private HttpEntity<Map<String, String>> createHttpRequest(Map<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(map, headers);
    }
}
