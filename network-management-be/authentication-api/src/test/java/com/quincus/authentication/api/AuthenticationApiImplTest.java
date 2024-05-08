package com.quincus.authentication.api;

import com.quincus.authentication.exception.AuthenticationApiException;
import com.quincus.authentication.model.AuthenticationUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationApiImplTest {
    @InjectMocks
    AuthenticationApiImpl authenticationApi;
    @Mock
    AuthenticationRestService authenticationRestService;

    @Test
    void login_Success() {
        String userName = "testUser";
        String password = "testPassword";
        String organizationId = "testOrgId";
        AuthenticationUser expectedAuthUser = new AuthenticationUser();

        when(authenticationRestService.login(userName, password, organizationId)).thenReturn(expectedAuthUser);

        AuthenticationUser result = authenticationApi.login(userName, password, organizationId);

        assertThat(result).isEqualTo(expectedAuthUser);

        verify(authenticationRestService).login(userName, password, organizationId);
        verifyNoMoreInteractions(authenticationRestService);
    }

    @Test
    void login_ExceptionThrown_ShouldThrowAuthenticationApiException() {
        String userName = "testUser";
        String password = "testPassword";
        String organizationId = "testOrgId";

        when(authenticationRestService.login(userName, password, organizationId)).thenThrow(new AuthenticationApiException("Error"));

        assertThatExceptionOfType(AuthenticationApiException.class)
                .isThrownBy(() -> authenticationApi.login(userName, password, organizationId))
                .withMessageContaining("Error");

        verify(authenticationRestService).login(userName, password, organizationId);
        verifyNoMoreInteractions(authenticationRestService);
    }

    @Test
    void validateToken_Success() {
        String token = "testToken";
        AuthenticationUser expectedAuthUser = new AuthenticationUser();

        when(authenticationRestService.validateToken(token)).thenReturn(expectedAuthUser);

        AuthenticationUser result = authenticationApi.validateToken(token);

        assertThat(result).isEqualTo(expectedAuthUser);

        verify(authenticationRestService).validateToken(token);
        verifyNoMoreInteractions(authenticationRestService);
    }

    @Test
    void validateToken_ExceptionThrown_ShouldThrowAuthenticationApiException() {
        String token = "testToken";

        when(authenticationRestService.validateToken(token)).thenThrow(new AuthenticationApiException("Error"));

        assertThatExceptionOfType(AuthenticationApiException.class)
                .isThrownBy(() -> authenticationApi.validateToken(token))
                .withMessageContaining("Error");

        verify(authenticationRestService).validateToken(token);
        verifyNoMoreInteractions(authenticationRestService);
    }
}
