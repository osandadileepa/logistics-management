package com.quincus.authentication.api;

import com.quincus.authentication.model.AuthenticationUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationApiImplTest {
    @InjectMocks
    AuthenticationApiImpl authenticationApi;
    @Mock
    AuthenticationRestService authenticationRestService;

    @Test
    void login_ShouldReturnResult() {
        when(authenticationRestService.login(anyString(), anyString(), anyString())).thenReturn(mock(AuthenticationUser.class));
        assertThat(authenticationApi.login("dummyUserName", "dummyPassword", "dummyOrg")).isNotNull();
    }

    @Test
    void login_ShouldReturnResultWhenOrganizationIdIsNull() {
        when(authenticationRestService.login(anyString(), anyString(), any())).thenReturn(mock(AuthenticationUser.class));
        assertThat(authenticationApi.login("dummyUserName", "dummyPassword", null)).isNotNull();
    }

    @Test
    void validateToken_ShouldReturnResult() {
        when(authenticationRestService.validateToken(anyString())).thenReturn(mock(AuthenticationUser.class));
        assertThat(authenticationApi.validateToken("dummyToken")).isNotNull();
    }
}
