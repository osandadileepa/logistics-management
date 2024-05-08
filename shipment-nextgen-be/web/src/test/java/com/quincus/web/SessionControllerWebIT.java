package com.quincus.web;

import com.quincus.web.common.web.BaseControllerWebIT;
import com.quincus.web.controller.SessionControllerImpl;
import com.quincus.web.dto.AuthenticationRequest;
import com.quincus.web.security.AuthenticationClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {SessionControllerImpl.class})
@ContextConfiguration(classes = {SessionControllerImpl.class})
class SessionControllerWebIT extends BaseControllerWebIT {
    private static final String SESSION_URL = "/sessions";
    @MockBean
    private AuthenticationClient authenticationClient;

    @Test
    @DisplayName("Given a user, when a valid AuthenticationRequest is provided, the response should be OK.")
    void givenAuthenticationRequestWhenValidDataThenShouldReturnSuccess() throws Exception {
        // GIVEN
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setOrganizationId(UUID.randomUUID().toString());
        authenticationRequest.setUsername("troa.starrr");
        authenticationRequest.setPassword("aPasswordForThisTe$t");
        authenticationRequest.setToken("a token");

        // WHEN
        final MvcResult result = performPostRequest(SESSION_URL, objectMapper.writeValueAsString(authenticationRequest));

        // THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given a user, when an invalid AuthenticationRequest is provided, the response should be BAD REQUEST.")
    void givenInvalidAuthenticationRequestWhenValidDataThenShouldReturnBadRequest() throws Exception {
        // GIVEN
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setOrganizationId("invalid id");
        authenticationRequest.setUsername(RandomStringUtils.randomAlphabetic(255 + 1));
        authenticationRequest.setPassword(RandomStringUtils.randomAlphabetic(255 + 1));
        authenticationRequest.setToken(RandomStringUtils.randomAlphabetic(2000 + 1));

        // WHEN
        final MvcResult result = performPostRequest(SESSION_URL, objectMapper.writeValueAsString(authenticationRequest));

        // THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

}